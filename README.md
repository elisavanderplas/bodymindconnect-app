# Body-Mind Connect (Bodymindconnect)

This repository contains the experimental tasks, analysis code, and documentation for the "Body-Mind Connect" project. The study investigates the bidirectional link between physiological states and cognitive/emotional processing.

## Project Overview
The project aims to quantify how interoceptive accuracy and physiological fluctuations influence decision-making and subjective experience. It utilizes a combination of behavioral tasks and physiological monitoring.

## Folder Structure

- `/Experiment`: Contains the PsychoPy experiment files. 
    - `task.psyexp`: The main experiment builder file.
    - `task.py`: The compiled Python script for running the experiment.
- `/Preprocessing`: Scripts for cleaning and preparing raw physiological data.
- `/Analysis`: R scripts for statistical analysis and data visualization.
- `/Pre-registration`: Documentation of the study design and hypotheses.

## Requirements

### To run the Experiment:
- [PsychoPy](https://www.psychopy.org/) (version 2021.x or higher recommended)
- Python 3.8+

### To run the Analysis:
- [RStudio](https://rstudio.com/)
- Required R packages: `tidyverse`, `lme4`, `brms` (for Bayesian analysis), `ggplot2`.

## How to Use

1. **Running the Task**:
   - Open `Experiment/task.psyexp` in PsychoPy.
   - Click "Run" to start the behavioral session.
   - Data will be saved automatically to a `data/` subfolder.

2. **Analyzing Data**:
   - Navigate to the `Analysis/` folder.
   - Run the preprocessing scripts first to clean the raw output.
   - Execute the `.Rmd` files to generate statistical results and figures.

## Author
**Elisa van der Plas** [GitHub Profile](https://github.com/elisavanderplas)

## License
This project is licensed under the MIT License - see the LICENSE file for details.
