```markdown
# Borda's Voting Method

This is a university project for implementing Borda's Voting Method on an Android application. It is an individual project,
and all development has been done solely by myself.

## Project Description

The application allows users to conduct a Borda count voting process.
Users can input options and vote on them using a seek bar to assign scores.
The application ensures that each option receives a unique score and calculates the results accordingly.

## Features

- Input the number of voting options.
- Enter all voting options.
- Add votes using seek bars to assign scores.
- View the number of votes so far.
- Reset the voting process.
- Display voting results based on Borda's count.

## Installation

To run this project, follow these steps:

1. Clone the repository:
   ```sh
   git clone https://github.com/mubahilahmad/bordasvotingmethod.git
   ```
2. Open the project in Android Studio.
3. Build the project and run it on an Android device or emulator.

## Usage

1. **Input Number of Options**: Enter the number of options for the voting process.
2. **Enter All Options**: Provide the voting options separated by commas.
3. **Add a Vote**: Assign scores to each option using the seek bars. Ensure that each option has a unique score.
4. **View Results**: Toggle the switch to view the voting results. The results will display the options along with their respective scores.
5. **Reset Voting**: Use the "Start Over" button to reset the voting process.

## Code Overview

### MainActivity

- Handles the main interface for inputting the number of options and the voting options.
- Manages the voting process and displays the number of votes so far.
- Starts the `VoteActivity` for adding votes.

### VoteActivity

- Displays the voting interface with dynamic seek bars for assigning scores to each option.
- Ensures that each option receives a unique score.
- Calculates and displays the voting results.

### XML Layout Files

- `activity_main.xml`: Layout for the main activity interface.
- `activity_vote.xml`: Layout for the voting activity interface.

## Screenshots


## Screenshots

### Main Activity
![Main Activity](screenshots/1_main_resized.png)
![Main Activity](screenshots/4_main_resized.png)

### Voting Activity
![Voting Activity](screenshots/2_main_resized.png)
![Voting Activity](screenshots/3_main_resized.png)


