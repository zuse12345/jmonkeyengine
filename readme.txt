$Id$

This file has to do only with branching and CM.  See the doc subdirectory for
design and implementation documentation.

This tentative intention is that a copy of this directory will eventually be
grafted onto the trunk root, something like

    # From root directory of a .../jme/trunk work area
    svn mkdir blenderjme
    cd blenderjme
    svn merge http.../branches/blenderjme blenderjme

This is a little non-traditional, but it will make it very unambiguous that
this branch includes no modifications outside of the blenderjme directory
branch, and browsing for blenderjme mods in this branch will be unencumbered
by copies of everything else in trunk.

[To be explicit, the reason it is a little non-traditional is that branches
typically mirror everything under trunk.  In this case, I don't think the
useless clutter of all of the trunk artifacts justifies conforming.]
