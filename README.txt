Knime provides powerful and flexible means to mine data. However, screening data requires some particular methods, that should be usable with little effort for daily analysis tasks. Therefore the High Content Screening Tools (HCS-Tools) come with a set of nodes to make the life of "screeners" easier.



USEFUL LINKS

The framework these plugins are for: www.knime.org



INSTALLATION

Once KNIME is installed you have the following possibilities:
1) The easiest is to use the p2 update mechanism of KNIME (Help > Install new Software). Find the detailed instructions on the KNIME community site: site: http://tech.knime.org/community-contributions-info.
2) Use eclipse to build the plugins yourself and add them to the plugin directory of the KNIME installation.



DEVELOPMENT

Since KNIME is an Eclipse application it is easiest to use that IDE.
1) Clone knime-scripting repository (https://github.com/knime-mpicbg/knime-scripting)
2) Clone this repository
3) In Eclipse Import>General>Existing Project into Workspace
4) Make sure the project is compiled with Java SE 1.6 (Preferences>Compiler)

Debug Configuration:
Main Tab:
	Run a Product: org.knime.product.KNIME_PRODUCT
	Runtime JRE: 1.6
Arguments Tab:
	Add “-XX:MaxPermSize=256m “ to the VM Arguments



LICENSE

Copyright (c) 2010, Max Planck Institute of Molecular Cell Biology and Genetics, Dresden
All rights reserved.

The HCS-Tools are distributed under GPL3 license.