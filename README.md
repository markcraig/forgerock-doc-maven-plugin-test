# ForgeRock doc-build-plugin-test

The ForgeRock [doc-build-plugin](https://github.com/markcraig/doc-build-plugin)
provides a centralized solution to build core documentation output.

This test project runs the plugin on a trivial documentation set designed to
allow the plugin maintainer quickly to check whether something is broken,
without building a complete ForgeRock documentation set.

To run the tests:

1.  Run `mvn site`. 
2.  Review the output under `target/site/doc/`. Check at least .pdf as well as
    single page and chunked HTML.

* * *
This work is licensed under the Creative Commons
Attribution-NonCommercial-NoDerivs 3.0 Unported License.
To view a copy of this license, visit
<http://creativecommons.org/licenses/by-nc-nd/3.0/>
or send a letter to Creative Commons, 444 Castro Street,
Suite 900, Mountain View, California, 94041, USA.

Copyright 2012 ForgeRock AS
