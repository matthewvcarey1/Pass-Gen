# An application to generate possible passwords

![screen shot](passgen.png "Running on Windows")


The file src/main/resources/word.txt has been generated from linux /usr/share/dict/words like this:

    grep -E '^[a-z|A-Z]{4,6}$' /usr/share/dict/words > src/main/resources/words.txt

Licence GNU Public Licence 3 https://www.gnu.org/licenses/gpl-3.0.en.html

This is a minimal Intellij Maven project

To build you need to have Maven installed and Java JDK version 21 or later.

To build:

    mvn clean package

To run

    java -jar target/PassGen-1.0-SNAPSHOT.jar

To put as a desktop shortcut in Windows, create shortcut on your desktop with a target of:

    C:\Users\YOURNAME\.jdks\openjdk-23.0.2\bin\javaw.exe -jar C:\Users\YOURNAME\Downloads\PassGen-1.0-SNAPSHOT.jar

(if your javaw.exe is at that location and the PassGen jar is in Downloads)    

To put as a desktop shortcut in Linux create a text file called passgen.desktop on the desktop like this:

    [Desktop Entry]
    Type=Application
    Name=Password Generator
    Comment=Generate secure 3-word passwords
    # Point this to your JAR file
    Exec=java -jar /home/YOURNAME/Downloads/PassGen-1.0-SNAPSHOT.jar  
    Icon=security-high
    Terminal=false
    Categories=Utility

