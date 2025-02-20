set VENV_DIR=venv

if not exist "%VENV_DIR%" (
    python -m venv %VENV_DIR%
    %VENV_DIR%\Scripts\python.exe -m pip install --upgrade pip
    if exist requirements.txt (
        %VENV_DIR%\Scripts\pip.exe install -r requirements.txt
    )
)

%VENV_DIR%\Scripts\python.exe generator.py