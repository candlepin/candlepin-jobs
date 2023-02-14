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
  echo "$@" | awk -F. '{ printf("%d%03d%03d%03d\n", $1,$2,$3,$4); }'
}

git fetch --all

for GIT_BRANCH in master candlepin-4.1-HOTFIX candlepin-4.2-HOTFIX; do

  #Clean the project
  ./gradlew clean
  evalrc $? "Project cleaning was not successful for branch $GIT_BRANCH."

  #Checkout the branch
  git checkout $GIT_BRANCH
  evalrc $? "Checkout branch : $GIT_BRANCH was not successful."

  JAVA_VERSION=11
  sudo update-alternatives --set java /usr/lib/jvm/java-$JAVA_VERSION-openjdk-$JAVA_VERSION*/bin/java

  #To generate the auto generated classes and files
  ./gradlew war
  evalrc $? "Compilation was not successful for branch $GIT_BRANCH."

  #Execute the gettext task
  ./gradlew gettext
  evalrc $? "Gettext was not successful for branch $GIT_BRANCH."

  files=$(git diff | egrep -v -e '^( |\+#|\-#|@@|\+\+\+|\-\-\-|diff|index)' -e 'X-Generator' -e 'POT-Creation-Date')

  if [ ! -z "$files" ]; then

    #Code to commit the updated template file
    git add po/keys.pot
    evalrc $? "Git add file was not successful for branch $GIT_BRANCH."

    echo "Committing and pushing the template file."
    git -c "user.name=$GIT_AUTHOR_NAME" -c "user.email=$GIT_AUTHOR_EMAIL" commit -m "updated po/keys.pot template"
    evalrc $? "Git commit was not successful for branch $GIT_BRANCH."

    git push https://${GITHUB_TOKEN}@github.com/candlepin/candlepin $GIT_BRANCH
    evalrc $? "Git push was not successful for branch $GIT_BRANCH."

  else
    #To stash the unnecessary changes
    git stash
  fi

done
