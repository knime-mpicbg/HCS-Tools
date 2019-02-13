Knime provides powerful and flexible means to mine data. However, screening data
requires some particular methods, that should be usable with little effort for
daily analysis tasks. Therefore the High Content Screening Tools (HCS-Tools)
come with a set of nodes to make the life of "screeners" easier.



## Useful Links
* [The KNIME framework](www.knime.org)
* [KNIME community contributions](https://www.knime.com/community)
* [HCS Tools Wiki](https://github.com/knime-mpicbg/HCS-Tools/wiki)

## Development
Since KNIME is an Eclipse application it is easiest to use that IDE. Follow the instruction on [KNIME SDK](https://github.com/knime/knime-sdk-setup) repository to install and confige Eclipse for KNIME development.


To work on this project use `File → Import → Git → Projects from Git File → Clone URI` and enter this repositorie's URL.


### Debug Configuration:

In the main menu of Eclipse go to `Run → Debug Configurations... → Eclipas Application → KNIME Analytics Platform` and hit `Debug`.

You might want to change the memory settings in the `Arguments` tab of the debug configuration by adding:

    -XX:MaxPermSize=256m


## Installation
Once [KNIME Analytics Platform](https://www.knime.com/knime-software/knime-analytics-platform) is installed you have the following possibilities:

1. The easiest is to use the p2 update mechanism of KNIME (Help > Install new Software). Find the detailed instructions on the [HCS Tools](https://www.knime.com/community/hcs-tools) page of the KNIME Community Contributions website.
2. Use eclipse to build the plugins yourself and add them to the plugin directory of the KNIME installation.
