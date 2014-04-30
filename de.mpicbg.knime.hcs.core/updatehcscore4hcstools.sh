#!/bin/sh

hcstoolsRoot=$1
echo "Working directory:"`pwd`
echo " "
# udpates the hcscore-jar in the hcs-tools knime package

mvn clean
mvn package -Dmaven.test.skip=true
mvn install -Dmaven.test.skip=true

rm -rf target/dependency
mvn dependency:copy-dependencies

cp target/dependency/* $hcstoolsRoot/lib/
cp target/hcscore-1.1.jar $hcstoolsRoot/lib/

# remove the duplicated poi-dependencies
#rm ../../knime/hcstools/lib/poi-*
#rm ../../knime/hcstools/lib/geronimo-*
#rm ../../knime/hcstools/lib/stax-*
#rm ../../knime/hcstools/lib/xmlbeans-*
#rm ../../knime/hcstools/lib/ejb3-persist*