from fernos.storage import FernMiniIO

def get_storage():
    """Shared utility to initialize storage."""
    return FernMiniIO()
