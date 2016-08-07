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
	echo "base_path: $(realpath '../../')" > 'crowdin.yaml'
	cat "../../crowdin.yaml" >> "crowdin.yaml"
fi

if grep -q "DONT_PUSH" "crowdin.yaml" ; then
	echo "Insert API key to crowdin.yaml"
	echo "and update folder to the root folder of the repository"
	exit
fi

# Load latest translations
crowdin-cli download -b master
