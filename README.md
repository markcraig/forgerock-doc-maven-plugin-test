# ForgeRock Doc Build Maven Plugin Tests

The ForgeRock [forgerock-doc-maven-plugin](https://github.com/markcraig/forgerock-doc-maven-plugin)
provides a centralized solution to build core documentation output.

This test project runs the plugin on a trivial documentation set designed to
allow the plugin maintainer quickly to check whether something is broken,
without building a complete ForgeRock documentation set.

To run the basic tests:

1.  Run `mvn site`. 
2.  Review the output under `target/site/doc/`. Check at least .pdf as well as
    single page and chunked HTML.

To run the release test:

1.  Turn off draft mode, provide a release version, and specify the
    docs.forgerock.org Goodle Analytics ID for the build:

        mvn -DisDraftMode=no -DreleaseVersion=1.0.0 -D"gaId=UA-23412190-14" \
        -D"releaseDate=Software release date: January 1, 1970" \
        clean site org.forgerock.commons:forgerock-doc-maven-plugin:release

2.  Review the output under `target/release/`.

* * *
This work is licensed under the Creative Commons
Attribution-NonCommercial-NoDerivs 3.0 Unported License.
To view a copy of this license, visit
<http://creativecommons.org/licenses/by-nc-nd/3.0/>
or send a letter to Creative Commons, 444 Castro Street,
Suite 900, Mountain View, California, 94041, USA.

Copyright 2012 ForgeRock AS
