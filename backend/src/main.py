from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
import uuid
import asyncio
from datetime import datetime
from typing import Dict, List
import uvicorn

app = FastAPI(title="Karaoke Backend", version="1.0.0")

# In-memory storage for demo
processing_jobs: Dict[str, dict] = {}

@app.get("/api/v1/health")
async def health_check():
    """Health check endpoint"""
    return {
        "success": True,
        "message": "Server is running",
        "timestamp": datetime.now().isoformat()
    }

@app.post("/api/v1/upload")
async def upload_audio(file: UploadFile = File(...), ai_model: str = "demucs"):
    """Upload audio file for processing"""
    
    # Validate file type
    if not file.content_type.startswith('audio/'):
        raise HTTPException(status_code=400, detail="Invalid file type")
    
    # Generate job ID
    job_id = str(uuid.uuid4())
    
    # Store job info
    processing_jobs[job_id] = {
        "job_id": job_id,
        "status": "PENDING",
        "progress": 0,
        "ai_model": ai_model,
        "filename": file.filename,
        "created_at": datetime.now().isoformat(),
        "updated_at": datetime.now().isoformat()
    }
    
    # Start mock processing
    asyncio.create_task(mock_processing(job_id))
    
    return processing_jobs[job_id]

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

@app.post("/api/v1/processing/{job_id}/cancel")
async def cancel_processing(job_id: str):
    """Cancel processing job"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    processing_jobs[job_id]["status"] = "CANCELLED"
    processing_jobs[job_id]["updated_at"] = datetime.now().isoformat()
    
    return {"success": True, "message": "Job cancelled"}

@app.get("/api/v1/processing/{job_id}/vocals")
async def download_vocals(job_id: str):
    """Download vocals file (mock)"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = processing_jobs[job_id]
    if job["status"] != "COMPLETED":
        raise HTTPException(status_code=400, detail="Processing not completed")
    
    # Return mock file content
    return JSONResponse({"message": "Vocals file download", "job_id": job_id})

@app.get("/api/v1/processing/{job_id}/instrumental")
async def download_instrumental(job_id: str):
    """Download instrumental file (mock)"""
    
    if job_id not in processing_jobs:
        raise HTTPException(status_code=404, detail="Job not found")
    
    job = processing_jobs[job_id]
    if job["status"] != "COMPLETED":
        raise HTTPException(status_code=400, detail="Processing not completed")
    
    # Return mock file content  
    return JSONResponse({"message": "Instrumental file download", "job_id": job_id})

async def mock_processing(job_id: str):
    """Mock processing simulation"""
    
    if job_id not in processing_jobs:
        return
    
    job = processing_jobs[job_id]
    
    try:
        # Simulate processing stages
        stages = [
            ("UPLOADING", 10),
            ("PROCESSING", 30),
            ("PROCESSING", 60),
            ("PROCESSING", 90),
            ("COMPLETED", 100)
        ]
        
        for status, progress in stages:
            if job["status"] == "CANCELLED":
                return
                
            job["status"] = status
            job["progress"] = progress
            job["updated_at"] = datetime.now().isoformat()
            
            if status == "PROCESSING":
                job["current_step"] = f"Processing with {job['ai_model']}"
            
            # Wait before next stage
            await asyncio.sleep(2)
        
        # Mark as completed
        if job["status"] != "CANCELLED":
            job["status"] = "COMPLETED"
            job["progress"] = 100
            job["updated_at"] = datetime.now().isoformat()
            job["vocals_ready"] = True
            job["instrumental_ready"] = True
            
    except Exception as e:
        job["status"] = "FAILED"
        job["error_message"] = str(e)
        job["updated_at"] = datetime.now().isoformat()

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)