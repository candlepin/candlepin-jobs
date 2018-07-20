virtualenv env --system-site-packages || true
source env/bin/activate
make install-pip-requirements
if [ -d python-rhsm ]; then
  pushd python-rhsm
fi
python setup.py build_ext --inplace
cd $WORKSPACE
sudo -i bash -c "cd $WORKSPACE; PYTHONPATH=$WORKSPACE/src:$WORKSPACE/python-rhsm/src nosetests -c playpen/noserc.zypper test/zypper_test"
shell 'sudo chown -R $USER $WORKSPACE'  # since we just ran w/ sudo
