# needs python-nose installed
# needs an xserver or vncserver running
# see http://www.oracle-base.com/articles/linux/configuring-vnc-server-on-linux.php for an example for f16
# needs polib installed, http://pypi.python.org/pypi/polib
# probably will need coverage tools installed
# systemctl start vncserver@:3.service
# systemctl stop vncserver@:3.service
# needs python-rhsm
# needs mock  (easy_install mock)
# needs PyXML installed
# needs pyflakes insalled
# if we haven't installed/ran subsctiption-manager (or installed it)
#   we need to make /etc/pki/product and /etc/pki/entitlement

#env

echo "sha" "${sha1}"


sudo yum-builddep subscription-manager.spec  # ensure we install any missing rpm deps
virtualenv env -p python3 --system-site-packages || true
source env/bin/activate
make install-pip-requirements

# so we can run these all everytime, we don't actually fail on each step, so checkout for output
#TMPFILE=`mktemp`|| exit 1; $(make stylish | tee $TMPFILE); if [ -s $TMPFILE ] ; then echo "FAILED"; cat $TMPFILE; exit 1; fi

if [ -d python-rhsm ]; then
  pushd python-rhsm
  UNIT_TEST_DIR=test/unit
else
  UNIT_TEST_DIR=test/rhsm/unit
fi
echo $UNIT_TEST_DIR
PYTHON_RHSM=$(pwd)

# build the c modules
python3 setup.py build
python3 setup.py build_ext --inplace

# not using "setup.py nosetests" yet
# since they need a running candlepin

# Run just the unit tests, functional needs a running candlepin
#pushd test/unit
nosetests --with-xunit --with-cover --cover-package rhsm --cover-erase $UNIT_TEST_DIR

SRC_DIR=$PYTHON_RHSM/src/rhsm/
coverage3 html --include "${SRC_DIR}/*"
coverage3 xml --include "${SRC_DIR}/*"
