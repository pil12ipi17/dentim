from fastapi import FastAPI, File, UploadFile, HTTPException, Form, status
from fastapi.responses import JSONResponse, FileResponse
import uuid
import asyncio
from datetime import datetime
from typing import Dict
from pathlib import Path
import os
import subprocess
import sys
import logging

import aiofiles
import uvicorn

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Karaoke Backend", version="1.0.0")

# In-memory storage for demo
processing_jobs: Dict[str, dict] = {}

BASE_DIR = Path(__file__).resolve().parent.parent
UPLOAD_DIR = BASE_DIR / "temp_uploads"
OUTPUT_DIR = BASE_DIR / "outputs"
MAX_FILE_SIZE = 100 * 1024 * 1024  # 100MB

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

@app.get("/health")
async def health_check():
    """Health check endpoint (contract)"""
    return {
        "success": True,
        "message": "Server is running",
        "timestamp": datetime.now().isoformat()
    }

@app.get("/api/v1/health")
async def health_check_legacy():
    """Legacy health check endpoint kept for backward compatibility"""
    return {
        "success": True,
        "message": "Server is running",
        "timestamp": datetime.now().isoformat()
    }

@app.post("/api/v1/upload", status_code=status.HTTP_202_ACCEPTED)
async def upload_audio(
    file: UploadFile = File(...),
    ai_model: str = Form(default="demucs"),
):
    """Upload audio file for processing"""
    
    print(f"Upload request received:")
    print(f"- File name: {file.filename}")
    print(f"- Content type: {file.content_type}")
    print(f"- File size: {file.size}")
    print(f"- AI model: {ai_model}")

    # Validate file type
    if not file.content_type or not file.content_type.startswith("audio/"):
        print(f"ERROR: Invalid content type: {file.content_type}")
        raise HTTPException(status_code=400, detail=f"Invalid file type: {file.content_type}")

    # Simple validation of AI model
    ai_model_clean = ai_model.strip('"').lower()
    if ai_model_clean not in {"demucs", "spleeter"}:
        print(f"ERROR: Invalid AI model: {ai_model} (cleaned: {ai_model_clean})")
        raise HTTPException(status_code=400, detail=f"Invalid ai_model value: {ai_model}")

    # Generate job ID (taskId in API contract)
    job_id = str(uuid.uuid4())

    # Persist uploaded file to disk
    original_name = file.filename or "audio"
    _, ext = os.path.splitext(original_name)
    if not ext:
        ext = ".wav"

    input_path = UPLOAD_DIR / f"{job_id}{ext}"
    total_size = 0

    try:
        async with aiofiles.open(input_path, "wb") as out_file:
            while True:
                chunk = await file.read(1024 * 1024)
                if not chunk:
                    break
                total_size += len(chunk)
                if total_size > MAX_FILE_SIZE:
                    raise HTTPException(status_code=413, detail="File too large")
                await out_file.write(chunk)
    except Exception:
        if input_path.exists():
            input_path.unlink()
        raise

    # Store job info (internal representation)
    # Clean ai_model from quotes if needed
    clean_ai_model = ai_model.strip('"') if ai_model else "demucs"
    
    processing_jobs[job_id] = {
        "task_id": job_id,
        "status": "QUEUED",
        "progress": 0,
        "ai_model": clean_ai_model,
        "filename": original_name,
        "input_path": str(input_path),
        "output_dir": str(OUTPUT_DIR / job_id),
        "created_at": datetime.now().isoformat(),
        "started_at": None,
        "completed_at": None,
        "updated_at": datetime.now().isoformat(),
    }

    # Start Demucs processing in the background
    asyncio.create_task(process_with_demucs(job_id))

    # TaskCreatedResponse (contract)
    # For now use a static estimated duration; can be refined later
    return {
        "task_id": job_id,  # Use task_id for Android compatibility
        "job_id": job_id,  # Keep job_id for backward compatibility
        "taskId": job_id,  # Keep taskId for backward compatibility
        "status": "QUEUED",
        "progress": 0,  # Add missing progress field
        "ai_model": clean_ai_model,  # Use cleaned AI model field
        "filename": original_name,  # Add filename field
        "input_path": str(input_path),  # Add input path
        "output_dir": str(OUTPUT_DIR / job_id),  # Add output dir
        "created_at": datetime.now().isoformat(),  # Add creation timestamp
        "started_at": None,  # Add started timestamp
        "completed_at": None,  # Add completed timestamp
        "updated_at": datetime.now().isoformat(),  # Add update timestamp
        "current_step": None,  # Add current step
        "instrumental_path": None,  # Add paths
        "vocals_path": None,
        "vocals_ready": False,  # Add ready flags
        "instrumental_ready": False,
        "estimated_completion": None,  # Add estimated completion
        "error_message": None,  # Add error message
        "estimatedDuration": 180,
    }

@app.get("/api/v1/tasks/{taskId}/status")
async def get_task_status(taskId: str):
    """Get processing task status (contract endpoint)."""
    if taskId not in processing_jobs:
        raise HTTPException(status_code=404, detail="Task not found")

    job = processing_jobs[taskId]
    return {
        "taskId": taskId,
        "status": job["status"],
        "progress": job["progress"],
        "currentStep": job.get("current_step", "Processing"),
        "estimatedRemainingSeconds": 60 if job["status"] == "PROCESSING" else 0,
    }

@app.get("/api/v1/processing/{job_id}/status")
async def get_processing_status(job_id: str):
    """Get processing job status"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = processing_jobs[job_id]
    return {
        "job_id": job_id,
        "status": job["status"],
        "progress": job["progress"],
        "current_step": job.get("current_step"),
        "vocals_ready": job["status"] == "COMPLETED",
        "instrumental_ready": job["status"] == "COMPLETED",
        "error_message": job.get("error_message")
    }

@app.get("/api/v1/processing")
async def get_all_processing():
    """Get all processing jobs"""
    return list(processing_jobs.values())

@app.post("/api/v1/tasks/{taskId}/cancel")
async def cancel_task(taskId: str):
    """Cancel processing task (contract endpoint)."""
    if taskId not in processing_jobs:
        raise HTTPException(status_code=404, detail="Task not found")

    processing_jobs[taskId]["status"] = "CANCELLED"
    processing_jobs[taskId]["updated_at"] = datetime.now().isoformat()

    return {"success": True}

@app.post("/api/v1/processing/{job_id}/cancel")
async def cancel_processing(job_id: str):
    """Legacy cancel endpoint - delegates to contract endpoint."""
    return await cancel_task(job_id)

@app.get("/api/v1/processing/{job_id}/vocals")
async def download_vocals(job_id: str):
    """Download vocals file"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = processing_jobs[job_id]
    if job["status"] != "COMPLETED":
        raise HTTPException(status_code=400, detail="Processing not completed")
    
    vocals_path_str = job.get("vocals_path")
    if not vocals_path_str:
        raise HTTPException(status_code=404, detail="Vocals file not available")

    vocals_path = Path(vocals_path_str)
    if not vocals_path.exists():
        raise HTTPException(status_code=404, detail="Vocals file not found")

    return FileResponse(
        path=vocals_path,
        filename=vocals_path.name,
        media_type="audio/wav",
    )

@app.get("/api/v1/processing/{job_id}/instrumental")
async def download_instrumental(job_id: str):
    """Download instrumental file"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = processing_jobs[job_id]
    if job["status"] != "COMPLETED":
        raise HTTPException(status_code=400, detail="Processing not completed")
    
    instrumental_path_str = job.get("instrumental_path")
    if not instrumental_path_str:
        raise HTTPException(
            status_code=404,
            detail="Instrumental file not available",
        )

    instrumental_path = Path(instrumental_path_str)
    if not instrumental_path.exists():
        raise HTTPException(
            status_code=404,
            detail="Instrumental file not found",
        )

    return FileResponse(
        path=instrumental_path,
        filename=instrumental_path.name,
        media_type="audio/wav",
    )

@app.get("/api/v1/tasks/{taskId}/download")
async def download_task_instrumental(taskId: str):
    """Download processed instrumental (contract endpoint)."""
    if taskId not in processing_jobs:
        raise HTTPException(status_code=404, detail="Task not found")

    job = processing_jobs[taskId]
    if job["status"] != "COMPLETED":
        raise HTTPException(
            status_code=404,
            detail="Task not found or processing not completed",
        )

    instrumental_path_str = job.get("instrumental_path")
    if not instrumental_path_str:
        raise HTTPException(
            status_code=404,
            detail="Instrumental file not available",
        )

    instrumental_path = Path(instrumental_path_str)
    if not instrumental_path.exists():
        raise HTTPException(
            status_code=404,
            detail="Instrumental file not found",
        )

    return FileResponse(
        path=instrumental_path,
        filename=instrumental_path.name,
        media_type="audio/wav",
    )

async def process_with_demucs(job_id: str) -> None:
    """Run Demucs processing for a job in the background."""
    if job_id not in processing_jobs:
        return

    job = processing_jobs[job_id]

    try:
        if job.get("status") != "QUEUED":
            # Only process jobs that are still queued
            return

        now_iso = datetime.now().isoformat()
        job["status"] = "PROCESSING"
        job["progress"] = 0
        job["current_step"] = f"Processing with demucs"
        job["started_at"] = now_iso
        job["updated_at"] = now_iso

        input_path = Path(job["input_path"])
        output_dir = Path(job["output_dir"])
        output_dir.mkdir(parents=True, exist_ok=True)

        def run_demucs() -> None:
            try:
                # Import demucs modules directly - avoid demucs.audio due to lameenc
                import torch
                from demucs.pretrained import get_model
                from demucs.apply import apply_model
                import soundfile as sf
                import torchaudio
                
                # Update progress: Loading model (10%)
                job["progress"] = 10
                job["current_step"] = "Loading AI model..."
                job["updated_at"] = datetime.now().isoformat()
                logger.info("Loading htdemucs model...")
                model = get_model("htdemucs")
                
                # Update progress: Loading audio (20%)
                job["progress"] = 20
                job["current_step"] = "Loading audio file..."
                job["updated_at"] = datetime.now().isoformat()
                logger.info(f"Loading audio file: {input_path}")
                import soundfile as sf
                waveform_np, sample_rate = sf.read(str(input_path))
                
                # Update progress: Processing audio (30%)
                job["progress"] = 30
                job["current_step"] = "Preparing audio data..."
                job["updated_at"] = datetime.now().isoformat()
                
                # Convert to torch tensor and ensure correct shape [channels, time]
                if waveform_np.ndim == 1:  # mono
                    waveform_np = waveform_np[:, None]  # [time, 1]
                waveform = torch.from_numpy(waveform_np.T).float()  # [channels, time]
                
                # Resample if needed
                if sample_rate != model.samplerate:
                    job["progress"] = 40
                    job["current_step"] = "Resampling audio..."
                    job["updated_at"] = datetime.now().isoformat()
                    logger.info(f"Resampling from {sample_rate}Hz to {model.samplerate}Hz")
                    import torchaudio
                    resampler = torchaudio.transforms.Resample(sample_rate, model.samplerate)
                    waveform = resampler(waveform)
                
                # Ensure stereo
                if waveform.shape[0] == 1:
                    waveform = waveform.repeat(2, 1)
                elif waveform.shape[0] > 2:
                    waveform = waveform[:2]
                
                # Update progress: Starting separation (50%)
                job["progress"] = 50
                job["current_step"] = "Separating voices..."
                job["updated_at"] = datetime.now().isoformat()
                
                # Run separation
                logger.info(f"Starting Demucs separation on {input_path}")
                with torch.no_grad():
                    sources = apply_model(model, waveform[None])
                
                # Update progress: Separation completed (80%)
                job["progress"] = 80
                job["current_step"] = "Saving separated tracks..."
                job["updated_at"] = datetime.now().isoformat()
                
                # Create output paths
                model_output_dir = output_dir / "htdemucs"
                track_output_dir = model_output_dir / input_path.stem
                track_output_dir.mkdir(parents=True, exist_ok=True)
                
                # Save separated sources
                source_names = model.sources  # ['drums', 'bass', 'other', 'vocals']
                logger.info(f"Model sources: {source_names}")
                
                # Update progress: Saving vocals (85%)
                job["progress"] = 85
                job["current_step"] = "Saving vocals track..."
                job["updated_at"] = datetime.now().isoformat()
                
                # Save vocals
                if "vocals" in source_names:
                    vocals_idx = source_names.index("vocals")
                    vocals_file = track_output_dir / "vocals.wav"
                    vocals = sources[0, vocals_idx]  # [channels, time]
                    sf.write(str(vocals_file), vocals.detach().cpu().numpy().T, model.samplerate)
                    logger.info(f"Saved vocals to {vocals_file}")
                
                # Update progress: Saving instrumental (90%)
                job["progress"] = 90
                job["current_step"] = "Saving instrumental track..."
                job["updated_at"] = datetime.now().isoformat()
                
                # Save instrumental (combine all non-vocal sources)
                instrumental = torch.zeros_like(sources[0, 0])
                for i, source_name in enumerate(source_names):
                    if source_name != "vocals":
                        instrumental += sources[0, i]
                
                instrumental_file = track_output_dir / "no_vocals.wav"
                sf.write(str(instrumental_file), instrumental.detach().cpu().numpy().T, model.samplerate)
                logger.info(f"Saved instrumental to {instrumental_file}")
                
                # Update progress: Finalizing (95%)
                job["progress"] = 95
                job["current_step"] = "Finalizing..."
                job["updated_at"] = datetime.now().isoformat()
                
                logger.info(f"Demucs separation completed successfully")
                
            except Exception as e:
                logger.error(f"Error in Demucs processing: {str(e)}")
                raise
        
        await asyncio.to_thread(run_demucs)

        instrumental_path, vocals_path = _find_output_files(output_dir)

        job["instrumental_path"] = str(instrumental_path) if instrumental_path else None
        job["vocals_path"] = str(vocals_path) if vocals_path else None
        completed_iso = datetime.now().isoformat()
        job["status"] = "COMPLETED"
        job["progress"] = 100
        job["updated_at"] = completed_iso
        job["completed_at"] = completed_iso
        job["vocals_ready"] = vocals_path is not None
        job["instrumental_ready"] = instrumental_path is not None
    except Exception as exc:
        job["status"] = "ERROR"
        job["error_message"] = str(exc)
        job["updated_at"] = datetime.now().isoformat()

def _find_output_files(base_dir: Path):
    """Locate instrumental and vocals files produced by Demucs."""
    instrumental = None
    vocals = None

    if not base_dir.exists():
        return instrumental, vocals

    for path in base_dir.rglob("*"):
        if not path.is_file():
            continue
        name = path.name.lower()
        if "no_vocals" in name or "instrumental" in name:
            instrumental = path
        elif "vocals" in name:
            vocals = path

    return instrumental, vocals

@app.get("/api/v1/download/{task_id}/{file_type}")
async def download_processed_file(task_id: str, file_type: str):
    """Download processed vocals or instrumental file"""
    if task_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Task not found")
    
    job = processing_jobs[task_id]
    
    if job["status"] != "COMPLETED":
        raise HTTPException(status_code=400, detail="Processing not completed")
    
    file_path = None
    
    if file_type == "vocals":
        file_path = job.get("vocals_path")
        filename = f"{task_id}_vocals.wav"
    elif file_type == "instrumental":
        file_path = job.get("instrumental_path")
        filename = f"{task_id}_instrumental.wav"
    else:
        raise HTTPException(status_code=400, detail="Invalid file type. Use 'vocals' or 'instrumental'")
    
    if not file_path or not Path(file_path).exists():
        raise HTTPException(status_code=404, detail=f"{file_type.capitalize()} file not found")
    
    return FileResponse(
        path=file_path,
        filename=filename,
        media_type="audio/wav"
    )

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)