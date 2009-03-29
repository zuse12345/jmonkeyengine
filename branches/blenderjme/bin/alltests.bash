#!/bin/sh
# /bin/sh to accommodate MinGW.

PROGNAME="${0##*/}"

# $Id$
#
# Executes all standalone and Blender environment tests.
# Command line parameters are just passed through to Blender for the Blender
# environment tests, and have no effect on the standalone tests.
# This script have non-zero return status if anything fails, but will attempt
# to execute all tests.  I.e. when a test failure is encountered, it will
# proceed on, remembering to exit with non-zero status at the end.
#
# See the file "doc/testing.txt" for details.
#
# Copyright (c) 2009, Blaine Simpson and the jMonkeyEngine team
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#     * Redistributions in binary form must reproduce the above copyright
#       notice, this list of conditions and the following disclaimer in the
#       documentation and/or other materials provided with the distribution.
#     * Neither the name of the <organization> nor the
#       names of its contributors may be used to endorse or promote products
#       derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY Blaine Simpson and the jMonkeyEngine team
# ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL Blaine Simpson or the jMonkeyEngine team
# BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

shopt -s xpg_echo
set +u

Failout() {
    echo "Aborting $PROGNAME:  $*" 1>&2
    exit 1
}
[ -n "$TMPDIR" ] || TMPDIR=/tmp
TMPFILE="$TMPDIR/${PROGNAME%%.*}-$$.py"

PYTHONPROG=python
[ -n "$PYTHONHOME" ] && PYTHONPROG="$PYTHONHOME/bin/python"
type -t blender >&- || Failout 'Blender is not in your env search path'
"$PYTHONPROG" -c 'import unittest' ||
Failout "Your Python interpreter is missing, or does not support module 'unittest': $PYTHONPROG"

case "$0" in */*) SCRIPTRELDIR="${0%/*}";; *) SCRIPTRELDIR=".";; esac

[ -z "$PYTHONPATH" ] && [ -d "${SCRIPTRELDIR}/../src" ] && {
	export PYTHONPATH="${SCRIPTRELDIR}/../src"
	[ -n "$VERBOSE" ] && echo "PYTHONPATH set to '$PYTHONPATH'"
}

declare -i failures=0

"$PYTHONPROG" "${PYTHONPATH}/jmetest/xml.py" || ((failures = failures + 1))
"$PYTHONPROG" "${PYTHONPATH}/jmetest/esmath.py" || ((failures = failures + 1))

# This single test tests the dependencies of the Blender environment itself.
# We very particularly do not want to use an external Python interpreter,
# but, unfortunately, the current Linux distros of Blender do not have basic
# Python packages like "os", which we ned.
# This should be the only test (standalone or Blender-env) which does not use
# "testunit".  It's impossible to test imports with testunit.
echo "\nWatch for output from Blender env. scripts below.
It's impractical for this script to detect failures with Blender env. tests,
since Blender does not set a meaningful exit status.\n"
case "$(uname)" in
    Linux) "$SCRIPTRELDIR/blenderscript.bash" "${PYTHONPATH}/blendertest/modules.py";;
    *) PYTHONHOME=/dev/null "$SCRIPTRELDIR/blenderscript.bash" "${PYTHONPATH}/blendertest/modules.py";;
esac

# Will do this ASAP.
#"$SCRIPTRELDIR/blenderscript.bash" "${PYTHONPATH}/blendertest/script.py" ||
#((failures = failures + 1))

exit $failures
