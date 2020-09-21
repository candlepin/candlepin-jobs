# needs xorg-x11-server-Xvfb rpm installed
# needs python-rhsm

# "make jenkins" will install these via `pip install -r test-requirements.txt'
#  if it can
#  (either user pip config, or virtualenvs)
# needs python-nose installed
# needs polib installed, http://pypi.python.org/pypi/polib
# probably will need coverage tools installed
# needs mock  (easy_install mock)
# needs PyXML installed
# needs pyflakes insalled
# if we haven't installed/ran subsctiption-manager (or installed it)
#   we need to make /etc/pki/product and /etc/pki/entitlement

echo "sha1:" "${sha1}"

# Decide which package manager to use
which dnf || EXITCODE=$?
if [ $EXITCODE -eq 0 ]; then
    PM=dnf
else
    PM=yum
fi

cd $WORKSPACE

sudo $PM clean expire-cache
if [ $PM -eq 'dnf' ]; then
    sudo $PM builddep -y subscription-manager.spec  # ensure we install any missing rpm deps
    virtualenv env -p python3
else
    virtualenv env -p python2
fi
source env/bin/activate

make install-pip-requirements

# build/test python-rhsm
if [ -d $WORKSPACE/python-rhsm ]; then
  pushd $WORKSPACE/python-rhsm
fi
PYTHON_RHSM=$(pwd)

# build the c modules
python setup.py build
python setup.py build_ext --inplace

# not using "setup.py nosetests" yet
# since they need a running candlepin
# yeah, kind of ugly...
cp build/lib.linux-*/rhsm/_certificate.so src/rhsm/

pushd $WORKSPACE
export PYTHONPATH="$PYTHON_RHSM"/src

make set-versions
# capture exit status of 'make stylish' and not 'tee'
( set -o pipefail; make stylish | tee stylish_results.txt )
