#!/bin/bash -p

PROGNAME="${0##*/}"

# $Id$
#
# Cuts up an image file into  hor x vert sub-images, each of size dim x dim.
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

SYNTAX_MSG="$PROGNAME HOR VERT DIM inputfile.ext
For example, '$PROGNAME 2 3 100 penguin.png' would cut up 'penguin.png'
into 6 files named penguin-*.png, each of size 100 pix x 100 pix..
It would also write a preview HTML file to \$TMPDIR/penguin.html.

Specify 1 for HOR or VERT to tile in only one direction."

Failout() {
    echo "Aborting $PROGNAME:  $*" 1>&2
    exit 1
}

type -t convert >&- ||
Failout "The ImageMagick program 'convert' is not in your search path"
type -t identify >&- ||
Failout "The ImageMagick program 'identify' is not in your search path"

[ $# -eq 4 ] || {
    echo "$SYNTAX_MSG" 1>&2
    exit 2
}

[ -n "$TMPDIR" ] || TMPDIR=/tmp
H="$1"; shift
V="$1"; shift
DIM="$1"; shift
BASEFILE="$1"; shift
EXT="${BASEFILE##*.}"
[ "$BASEFILE" = "$EXT" ] &&
Failout "Input file must have a filename extension: $BASEFILE"
PREEXT="${BASEFILE%.*}"
TMPFILE="$TMPDIR/${PREEXT##*/}.html"

echo '<HTML>
<HEAD>
  <STYLE>
    table, tr, td, img { border:0; margin:0; padding:0; }
  </STYLE>
  <BODY>
    <TABLE cellspacing="0">' > "$TMPFILE" ||
Failout "Failed to write to preview file '$TMPFILE'"

((curV = 0))
while [ $curV -lt $V ]; do
    ((y = curV * DIM))
    ((curV = curV + 1))
    ((curH = 0))
    echo '      <TR>'
    while [ $curH -lt $H ]; do
        ((x = curH * DIM))
        ((curH = curH + 1))
        if [ $H -eq 1 ]; then
            OUTFILE="${PREEXT}-${curV}.${EXT}"
        elif [ $V -eq 1 ]; then
            OUTFILE="${PREEXT}-${curH}.${EXT}"
        else
            OUTFILE="${PREEXT}-${curH}x${curV}y.${EXT}"
        fi
        convert -crop "${DIM}x${DIM}+${x}+${y}"  \
          +repage "$BASEFILE" "$OUTFILE" 1>&2
        case "$OUTFILE" in /*);; *) OUTFILE="${PWD}/$OUTFILE";; esac
        echo "        <TD><IMG src='$OUTFILE'/></TD>"
    done
    echo '      </TR>'
done >> "$TMPFILE"

echo '    </TABLE>
  </BODY>
</HTML>' >> "$TMPFILE"
echo "See preview file '$TMPFILE'"
exec identify "$BASEFILE" "${PREEXT}"-*."${EXT}"
