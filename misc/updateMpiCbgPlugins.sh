#!/bin/sh


if [ "$1" = "beta" ]; then

    if [ -d "plugins/de.mpicbg.tds.knime.hcstools_1.0.0" ]; then
        echo "Cleaning up existing instance..."
        #remove the old instance of the plugin to get rid of outdated jars
        rm -r plugins/de.mpicbg.tds.knime.hcstools_1.0.0
    fi

    echo "updating plugins from beta-channel"
    cp -rX /Volumes/swengtools/datamining/knimetools/misc/devpreview/plugins/de.mpicbg.* ./plugins/

else

    if [ -d "plugins/de.mpicbg.tds.knime.hcstools_1.0.0" ]; then
        echo "Cleaning up existing instance..."
        #remove the old instance of the plugin to get rid of outdated jars
        rm -r plugins/de.mpicbg.tds.knime.hcstools_1.0.0
    fi

    echo "Installing latest stable version of mpi-cbg plugins..."

    cp -rX /Volumes/swengtools/datamining/knimetools/knime/plugins/de.mpicbg.* ./plugins/
fi

