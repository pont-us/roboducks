#!/bin/bash

set -e

pdflatex report
bibtex report
pdflatex report
pdflatex report
