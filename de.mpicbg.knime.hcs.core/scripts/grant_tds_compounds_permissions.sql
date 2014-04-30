GRANT SELECT ON hit, chemical, sirna, compound TO ""tdsuser""


select count(*)  from compound;
SELECT * FROM compound WHERE libraryCode='HGW';

-- DELETE FROM sirna USING compound WHERE sirna.id = compound.id AND libraryCode='HGW';
-- DELETE FROM compound WHERE libraryCode='HGW';

