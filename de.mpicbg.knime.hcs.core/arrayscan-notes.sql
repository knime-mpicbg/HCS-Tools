
-- Select all protcol-names which given a list of plates
SELECT protocol.ID, protocol.Name, protocol.protocolVersion, plate.CS_PlateID FROM protocol protocol, plate plate WHERE plate.protocolID = protocol.ID AND plate.CS_PlateID IN ('459', '3774')


-- Get all possible features for a set of plates (given by cs-plate-id)
SELECT protocolwellfeature.featureID, featureType.description, protocol.ID, protocol.Name as 'Protocol Name', protocol.protocolVersion, plate.CS_PlateID, plate.PlateBarCode as 'Plate Barcode'
FROM protocol protocol, plate plate, protocolwellfeature, featureType
WHERE plate.protocolID = protocol.ID
AND plate.CS_PlateID IN ('459', '460')
AND protocol.id = protocolwellfeature.protocolID
AND featureType.ID  = protocolwellfeature.featureID




-- planned workflow to query data from array-scan

- script node implementation for the first
- first select a protocol with a particuar version

- select which plates to r

working queries from martins workflow:

-- 1) read the plate given a barcode list   (iterate over barcode list)

SELECT plate.PlateBarCode, plate.name as 'plateName', plate.ScanId, plate.ID, plate.ImagePath, plate.cs_plateid,
protocol.name as 'protocolName', protocol.ProtocolVersion, protocol.objective, plate.PlateStartTime, plate.PlateFinishTime,
plate.WellCount, plate.WFieldCount, plate.FImageCount, plate.cellcount, plate.statusID FROM plate, protocol WHERE
plate.protocolID = protocol.ID
AND plate.PlateBarCode = '903EM090629A-DMS-FNF'

-- original
plate.protocolID = protocol.ID AND plate.PlateBarCode IN ('#PLACE_HOLDER_DO_NOT_EDIT#')



--2) read the wells given some plates (without features) (Loopling over cs-field id from query (1) )

SELECT plate.cs_plateid, plate.name, well.prow, well.pcol, wellfeature.valdbl, featuretype.description from plate, well,
 wellfeature, featuretype WHERE plate.cs_plateid=well.cs_plateid AND well.id=wellfeature.wellid AND
 featuretype.id=wellfeature.typeid AND (featuretype.DisplayName = 'ValidFieldCount' OR
  featuretype.DisplayName = 'MEAN_ObjectAvgIntenCh1') AND plate.cs_plateid=('#PLACE_HOLDER_DO_NOT_EDIT#')



-- 2) Read actual features from array-scan
 

SELECT wellfeature.valdbl as "wellValue", well.prow, well.pcol, wellfeature.typeid as "featureID", well.cs_plateid as "cs_plateid", protocol.name AS protocolName, plate.platebarcode
FROM well, wellfeature, protocol, plate
where wellfeature.wellid = well.id AND well.cs_plateid = plate.cs_plateid AND plate.protocolid = protocol.id



-- 3) get possible readouts


select featuretype.description as "featureDescription", featuretype.displayname AS "featureName", featuretype.id as "featureID", featuretype.notes as "comment", featuretype.featuretypetypeid as "featureLevel", protocol.assaymod as "bioApplication", plate.cs_plateid from featuretype, protocolselectedfeature, protocol, plate
where plate.protocolid = protocol.id and protocolselectedfeature.protocolid = protocol.id and
protocolselectedfeature.featureid = featuretype.id


-- test snippets

a) get all barcodes for a gvien protocol


a) get all barcodes and the corresponding cs_plate_id for a gvien protocol

SELECT plate.PlateBarCode, plate.cs_plateid, plate.name as 'plateName', plate.protocolID, protocol.ID, protocol.name
 FROM plate, protocol WHERE plate.protocolID = protocol.ID AND plate.PlateBarCode = '903EM090629A-DMS-FNF'

b) get all protocol-names


SELECT algorithmfeature.FeatureTypeID, featuretype.Description from algorithmfeature, protocol, featuretype 
where
protocol.name = 'MS_ME_EMT_Colony_10x_090412'  and
protocol.AssayMod = algorithmfeature.AssayModule and
algorithmFeature.FeatureTypeID  = featuretype.ID



