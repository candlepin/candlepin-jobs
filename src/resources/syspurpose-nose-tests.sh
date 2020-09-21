cd $WORKSPACE

# Do nothing if syspurpose directory is absent
if [ ! -d syspurpose ]; then
    exit 0;
fi

echo "sha1:" "${sha1}"

which dnf || EXITCODE=$?
if [ $EXITCODE -eq 0 ]; then
    PM=dnf
else
    PM=yum
fi

sudo $PM clean expire-cache
sudo $PM install -y python37
if [ $PM -eq 'dnf' ]; then
    sudo dnf builddep -y subscription-manager.spec  # ensure we install any missing rpm deps
fi

pushd $WORKSPACE/syspurpose

pip install --user pipenv
# Make pipenv available on the path
PATH=$PATH:"$(python -m site --user-base)/bin"

pipenv install --dev
pipenv run ./setup.py nosetests
