#!/bin/bash

VENV_DIR="venv"

if [ ! -d "$VENV_DIR" ]; then
    python3 -m venv "$VENV_DIR"
    "$VENV_DIR/bin/python" -m pip install --upgrade pip

    if [ -f "requirements.txt" ]; then
        "$VENV_DIR/bin/pip" install -r requirements.txt
    fi
fi

"$VENV_DIR/bin/python" plot.py
