#!/bin/sh
now=$(date +%s)
NEW_WAR=/home/ext_chris_mclain_gmail_com/B2FJavaServerMaster.war
if test -f "$NEW_WAR"; then
    chown tomcat8:tomcat8 /home/ext_chris_mclain_gmail_com/B2FJavaServerMaster.war 
    echo "changed war permissions"
    WAR_LOC=/opt/tomcat/webapps/
    ROOT_WAR="${WAR_LOC}/ROOT.war"
    if test -f "$ROOT_WAR"; then
        mv "$ROOT_WAR" "${WAR_LOC}/backups/B2FJavaServerMaster.war.${now}"
        echo "moved current war to ${WAR_LOC}/backups/B2FJavaServerMaster.war.${now}"
    fi
    mv "${NEW_WAR}"  "${ROOT_WAR}"
    echo "moved war to webapp dir"
    echo "Done."
else
    echo "there wasn't a new file where I thought it should be."
fi