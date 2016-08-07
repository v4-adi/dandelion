#!/bin/bash
#########################################################
#
#   Title
#
#   Created by Gregor Santer (gsantner), 2016
#   https://gsantner.github.io/
#
#########################################################


#Pfade
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
SCRIPTFILE=$(readlink -f $0)
SCRIPTPATH=$(dirname $SCRIPTFILE)
argc=$#

#########################################################
cd "$SCRIPTDIR"

if [ ! -f "crowdin.yaml" ] ; then
	echo "project_identifier: diaspora-for-android" > 'crowdin.yaml'
	echo "base_path: $(realpath '../../')" >>'crowdin.yaml'
	echo "api_key: DONT_PUSH_API_KEY" >>'crowdin.yaml'
	cat "../../crowdin.yaml" >> "crowdin.yaml"
	echo "# Add all non locality languages here" >> "crowdin.yaml"
	echo "# (e.g. enUS, enUK, deCH, deAT will automatically go into the right folder)" >> "crowdin.yaml"
	echo "# Otherwise e.g.  en would get added into the folder enEN (which is wrong)." >> "crowdin.yaml"
	echo "# https://crowdin.com/page/api/language-codes contains supported language codes" >> "crowdin.yaml"
	echo "# The first listed ones here are diffently managed by crowdin than on android" >> "crowdin.yaml"
fi

if grep -q "DONT_PUSH" "crowdin.yaml" ; then
	echo "Insert API key to crowdin.yaml"
	echo "and update folder to the root folder of the repository"
	exit
fi

# Load latest translations
crowdin-cli download -b master
