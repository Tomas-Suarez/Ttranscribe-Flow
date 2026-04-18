import os
import sys
import tempfile
from fastapi import FastAPI, UploadFile, File, HTTPException, Query
from faster_whisper import WhisperModel


if "VIRTUAL_ENV" in os.environ:
    base_path = os.environ["VIRTUAL_ENV"]
    nvidia_libs = [
        f"{base_path}/lib64/python3.14/site-packages/nvidia/cublas/lib",
        f"{base_path}/lib64/python3.14/site-packages/nvidia/cudnn/lib"
    ]
    os.environ["LD_LIBRARY_PATH"] = ":".join(nvidia_libs) + ":" + os.environ.get("LD_LIBRARY_PATH", "")


app = FastAPI(title="TranscribeFlow AI Engine")

try:
    model = WhisperModel("tiny", device="cuda", compute_type="float16")
    print("--- Model loaded successfully on CUDA ---")
except Exception as e:
    print(f"CUDA Loading Error: {e}\nFalling back to CPU...")
    model = WhisperModel("tiny", device="cpu", compute_type="int8")


@app.post("/transcribe")
async def transcribe_audio(
    file: UploadFile = File(...),
    language: str = Query("es", description="Language code (es, en, fr). Default is 'es'.")
):
    allowed_extensions = ('.mp3', '.wav', '.m4a', '.ogg', '.flac')
    if not file.filename.lower().endswith(allowed_extensions):
        raise HTTPException(status_code=400, detail="Unsupported file format")

    suffix = os.path.splitext(file.filename)[1]
    with tempfile.NamedTemporaryFile(delete=False, suffix=suffix) as temp_file:
        content = await file.read()
        temp_file.write(content)
        temp_path = temp_file.name

    try:
        print(f"Transcribing in language: {language}")
        segments, info = model.transcribe(temp_path, language=language, beam_size=5)

        full_text = " ".join([segment.text for segment in segments])

        return {
            "status": "success",
            "language_used": info.language,
            "transcription": full_text.strip()
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        if os.path.exists(temp_path):
            os.remove(temp_path)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)