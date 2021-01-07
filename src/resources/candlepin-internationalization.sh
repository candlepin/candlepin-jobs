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

for GIT_BRANCH in master candlepin-3.1-HOTFIX candlepin-2.9-HOTFIX
do

  #Clean the project
  ./gradlew clean
  evalrc $? "Project cleaning was not successful for branch $GIT_BRANCH."

  #Checkout the branch
  git checkout $GIT_BRANCH
  evalrc $? "Checkout branch : $GIT_BRANCH was not successful."

  # Check if we need to use Java 11, otherwise auto detect Java version.
  # Candlepin support Java 11 from 3.2.0 & onwards.

  CANDLEPIN_BASE_VERSION=$(grep "Version:" server/candlepin.spec.tmpl | awk -F"Version: " '{print $2}')
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

  #Executing the msgattrib task
  ./gradlew msgattrib
  evalrc $? "msgattrib was not successful for branch $GIT_BRANCH."

  files=$(git diff | egrep -v -e '^( |\+#|\-#|@@|\+\+\+|\-\-\-|diff|index)' -e 'X-Generator' -e 'POT-Creation-Date')

  if [  ! -z  "$files" ]; then

    #Code to commit the updated files
    git add common/po
    evalrc $? "Git add files was not successful for branch $GIT_BRANCH."

    echo "Committing and pushing the files."
    git -c "user.name=$GIT_AUTHOR_NAME" -c "user.email=$GIT_AUTHOR_EMAIL" commit -m "Internationalization: common/po files are updated"
    evalrc $? "Git commit was not successful for branch $GIT_BRANCH."

    git push https://${GITHUB_TOKEN}@github.com/candlepin/candlepin  $GIT_BRANCH
    evalrc $? "Git push was not successful for branch $GIT_BRANCH."

  else
    #To stash the unnecessary changes
    git stash
  fi

done
