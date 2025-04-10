#!/bin/bash
# set -x # Enable debugging output
# set -e # Exit on error

echo "Current working directory: $(pwd)"
ls -l /db # list content of /db
ls -l /setup # list content of /setup
echo
echo

# Change directory to /setup
cd /setup

echo "Changed working directory to: $(pwd)"
ls -l
echo
echo


DATAFILE='data_temp.json' # Use a temporary file name
touch "${DATAFILE}"
chown root:root "${DATAFILE}"

# Write header row directly (no comma needed here yet)
echo '[' > "${DATAFILE}"
echo '  ["code","type","name"]' >> "${DATAFILE}" # Added comma manually below
echo
echo

ucs=()
for ch in {A..Z}
do
  ucs=( "${ucs[@]}" "$ch" )
done

rand_parts=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 7)

TYPES=( 'catA' 'catB' 'catC' 'catD' 'catE' )

#  Loop to generate data rows 

MAX_ROWS=7

for (( i=0; i < MAX_ROWS ; i++ ))
do
  code="${ucs[$((RANDOM % 26))]}${ucs[$((RANDOM % 26))]}$RANDOM"
  type="${TYPES[$((RANDOM % 5))]}"
  name="${code} ${rand_parts[i]}" # name might contain newlines from fold

  # Base64 encoding logic
  if (( RANDOM % 64 == 0 )); then
    type="base64:$(echo -n "$type" | base64 -w 0)"
  fi
  if (( RANDOM % 8 == 0 )); then
    # remove newlines BEFORE base64 encoding if name was chosen
    name_no_newlines=$(echo "$name" | tr -d '\n') # Remove newlines first
    name="base64:$(echo -n "$name_no_newlines" | base64 -w 0)"
  else
    # Remove newlines from name even if not base64 encoding
    name=$(echo "$name" | tr -d '\n')
  fi

  # Comma logic
  if (( i > 0 || MAX_ROWS == 1 )); then
     # printf for the comma as well to avoid potential extra newline from echo
     printf "," >> "${DATAFILE}"
     # Add a newline for readability AFTER the comma
     printf "\n" >> "${DATAFILE}"
  else
     # Add comma after header if it's the only data row
     printf "," >> "${DATAFILE}"
     printf "\n" >> "${DATAFILE}"
  fi

  # Append the row data using printf
  printf '  ["%s", "%s", "%s"]' "$code" "$type" "$name" >> "${DATAFILE}"

 
done

# Add final newline and closing bracket
echo '' >> "${DATAFILE}" # Add newline for readability before bracket
echo ']' >> "${DATAFILE}"

echo "Temporary database created!"

# Copy data_temp.json to /db/data.json
cp "${DATAFILE}" /db/data.json

# Change ownership of /db/data.json
chown root:root /db/data.json


echo
echo
echo "data.json created in /db"
ls -l /db
echo
echo
