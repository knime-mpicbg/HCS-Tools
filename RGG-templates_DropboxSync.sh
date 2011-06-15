#!/bin/bash

# Set the working directory for the script execution to the corresponding Dropbox directory on your machine.
# To make it work smoothly, pass your public key to the subersion server.

echo ' '
export PATH=/usr/local/git/bin:$PATH
workingdir=`pwd`

cd ~/Downloads/
echo "downloading sources to "`pwd`
git clone --depth 1 https://github.com/knime-mpicbg/knime-scripting.git

echo " "
cd $workingdir
echo "working (destination) directory: "$workingdir
echo "updating R"
cp -r ~/Downloads/knime-scripting/r4knime/templates/ R
echo "updating groovy"
cp -r ~/Downloads/knime-scripting/groovy4knime/templates/ Groovy
echo "updating matlab"
cp -r ~/Downloads/knime-scripting/matlab4knime/templates/ Matlab
echo "updating Python"
cp -r ~/Downloads/knime-scripting/python4knime/templates/ Python

echo " "
echo "removing ~/Downloads/knime-scripting/"
rm -rf ~/Downloads/knime-scripting/


##source ~/.profile
#echo "updating groovy"
#svn --force export svn+ssh://$userName@subversion-srv3/tds/knime/groovy4knime/trunk/templates/ Groovy
#
#echo "updating matlab"
#svn --force export svn+ssh://$userName@subversion-srv3/tds/knime/matlab4knime/trunk/templates Matlab
#
#echo "updating R"
#svn --force export svn+ssh://$userName@subversion-srv3/tds/knime/r4knime/trunk/templates/ R
#
#echo "updating Python"
#svn --force export svn+ssh://$userName@subversion-srv3/tds/knime/python4knime/trunk/templates/ Python

