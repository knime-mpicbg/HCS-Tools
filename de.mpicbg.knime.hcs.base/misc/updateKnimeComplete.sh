#!/bin/sh


#remove the old instance of the plugin to get rid of outdated jars

echo "Cleaning up existing instance..."
rm -r plugins/*


echo "Installing new version of Knime..."


cp -rX /Volumes/swengtools/datamining/knimetools/knime/plugins/* ./plugins/

echo "done!"