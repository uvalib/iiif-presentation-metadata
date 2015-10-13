Java library for IIIF presentation API metadata
===============================================

This project consists of a package containing abstract
classes representing the major constructs in the IIIF
presentation metadata specificiation, that are convenient
starting points for generating such metadata.

http://iiif.io/api/presentation/2.0/

Furthemore, a single reference implementation that allows
IIIF presentation metadata to be exposed from resources
in the UVA digital library exists.

One can produce mimial useful IIIF presentation metadata
simply by extending the base abstract class 
(edu.virginia.lib.iiif.data.Manifiest).

Once complete, manifest JSON files can be created by
invoking the toJson() method on that class.

