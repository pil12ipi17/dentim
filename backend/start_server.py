#!/usr/bin/env python3

import uvicorn
import sys
import os

# Add the backend directory to Python path
backend_dir = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, backend_dir)

# Import the FastAPI app
from src.main import app

if __name__ == "__main__":
    uvicorn.run(
        app,
        host="127.0.0.1", 
        port=8000, 
        reload=False
    )