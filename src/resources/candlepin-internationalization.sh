#!/bin/bash -x

evalrc() {
    if [ "$1" -ne "0" ]; then
        echo "$2"
        exit $1
    fi
}

# This function converts candlepin version to a int value with (Zero) padding
# which helps in comparing versions.
# Examples -
# 2.5.6  -> 2005006000
# 3.2.0  -> 3002000000
# 2.9.6  -> 2009006000
get_simplified_version() {
	echo "$@" | awk -F. '{ printf("%d%03d%03d%03d\n", $1,$2,$3,$4); }';
}


git fetch --all

for GIT_BRANCH in master candlepin-4.0-HOTFIX candlepin-3.2-HOTFIX candlepin-3.1-HOTFIX
do

  #Clean the project
  ./gradlew clean
  evalrc $? "Project cleaning was not successful for branch $GIT_BRANCH."

  #Checkout the branch
  git checkout $GIT_BRANCH
  evalrc $? "Checkout branch : $GIT_BRANCH was not successful."

  # Candlpin project restructuring
  # We need to correctly set the project home path.
  # We define CP_NEW_STRUCTURE boolean to check if
  # we are running with new candlepin project structure.
  # In new candlepin project structure, candlepin.spec.tmpl
  # file is moved to project root.
  PROJECT_DIR="."
  CP_NEW_STRUCTURE=true
  if [ ! -f candlepin.spec.tmpl ]; then
    PROJECT_DIR="server"
    CP_NEW_STRUCTURE=false
    echo "This build is running with OLD Candlepin project structure."
  else
    echo "This build is running with NEW Candlepin project structure."
  fi

  # Check if we need to use Java 11, otherwise auto detect Java version.
  # Candlepin support Java 11 from 3.2.0 & onwards.

  CANDLEPIN_BASE_VERSION=$(grep "Version:" $PROJECT_DIR/candlepin.spec.tmpl | awk -F"Version: " '{print $2}')
  JAVA_11_SUPPORTED_VERSION=3.2.0

  echo "Base version of candlepin: " $CANDLEPIN_BASE_VERSION
  if [ $(get_simplified_version $CANDLEPIN_BASE_VERSION) -ge $(get_simplified_version $JAVA_11_SUPPORTED_VERSION) ] ; then
      JAVA_VERSION=11
      echo "Using Java 11 for branch $GIT_BRANCH."
      sudo update-alternatives --set java /usr/lib/jvm/java-$JAVA_VERSION-openjdk-$JAVA_VERSION*/bin/java
  else
      JAVA_VERSION=1.8.0
      echo "Using Java 8 for branch $GIT_BRANCH."
      sudo update-alternatives --set java /usr/lib/jvm/java-$JAVA_VERSION-openjdk-$JAVA_VERSION*/jre/bin/java
  fi

  #To generate the auto generated classes and files
  ./gradlew war
  evalrc $? "Compilation was not successful for branch $GIT_BRANCH."

  #Execute the gettext task
  ./gradlew gettext
  evalrc $? "Gettext was not successful for branch $GIT_BRANCH."

  files=$(git diff | egrep -v -e '^( |\+#|\-#|@@|\+\+\+|\-\-\-|diff|index)' -e 'X-Generator' -e 'POT-Creation-Date')

  if [  ! -z  "$files" ]; then

    #Code to commit the updated template file
    if [ "$CP_NEW_STRUCTURE" == true ]; then
      git add po/keys.pot
    else
      git add common/po/keys.pot
    fi
    evalrc $? "Git add file was not successful for branch $GIT_BRANCH."

    echo "Committing and pushing the template file."
    if [ "$CP_NEW_STRUCTURE" == true ]; then
      git -c "user.name=$GIT_AUTHOR_NAME" -c "user.email=$GIT_AUTHOR_EMAIL" commit -m "i18n: po/keys.pot template is updated"
    else
      git -c "user.name=$GIT_AUTHOR_NAME" -c "user.email=$GIT_AUTHOR_EMAIL" commit -m "i18n: common/po/keys.pot template is updated"
    fi

    evalrc $? "Git commit was not successful for branch $GIT_BRANCH."

    git push https://${GITHUB_TOKEN}@github.com/candlepin/candlepin  $GIT_BRANCH
    evalrc $? "Git push was not successful for branch $GIT_BRANCH."

  else
    #To stash the unnecessary changes
    git stash
  fi

done
