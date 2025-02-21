#!/bin/bash

VENV_DIR="venv"

PYTHON="$VENV_DIR/bin/python3"
PIP="$VENV_DIR/bin/pip"

if [ ! -d "$VENV_DIR" ]; then
    python3 -m venv "$VENV_DIR"
    source "$VENV_DIR/bin/activate"
    $PIP install -r requirements.txt
else
    source "$VENV_DIR/bin/activate"
fi

$PYTHON generator.py