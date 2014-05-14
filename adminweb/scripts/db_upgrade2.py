
# from 0.2.2 -> 0.3.0

# file storage changes, make filename full path relative to base storage location


import sys, pymysql, re, os, shutil

STORAGE_DIR = '/path/to/genemania/adminweb/storage'
NEW_STORAGE_DIR = '/path/to/genemania/adminweb/storage.new'

HOST = 'hostname'
USER = 'username'
PASSWD = 'password'
DB = 'dbname'
PORT = 3306

con = pymysql.connect(host=HOST, user=USER, passwd=PASSWD, db=DB, port=PORT)
cursor = con.cursor()

def alter_schema():
    sql = "ALTER TABLE  `NETWORKS` ADD  `DATA_FILE_ID` INT( 11 ) NULL;"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE  `IDENTIFIERS` ADD  `DATA_FILE_ID` INT( 11 ) NULL;"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE  `ATTRIBUTE_METADATA` ADD  `DATA_FILE_ID` INT( 11 ) NULL;"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE GROUPS ADD GROUP_TYPE VARCHAR(16) NOT NULL DEFAULT 'NETWORK'";
    cursor.execute(sql)
    con.commit()

    sql = "CREATE TABLE `DB_VERSION` (`id` INTEGER AUTO_INCREMENT , `VERSION_ID` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`) );"
    cursor.execute(sql)
    con.commit()

    sql = """
CREATE TABLE `FUNCTIONS` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `DATA_FILE_ID` int(11) DEFAULT NULL,
  `DESCRIPTION_FILE_ID` int(11) DEFAULT NULL,
  `FUNCTION_TYPE` varchar(16) NOT NULL,
  `COMMENT` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;
"""
    cursor.execute(sql)
    con.commit()

    sql = """
CREATE TABLE IF NOT EXISTS `DATAFILES` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ORGANISM_ID` int(11) NOT NULL,
  `FILENAME` varchar(128) DEFAULT NULL,
  `ORIGINAL_FILENAME` varchar(128) DEFAULT NULL,
  `FILE_TYPE` varchar(16) DEFAULT NULL,
  `UPLOAD_DATE` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `STATUS` varchar(16) DEFAULT NULL,
  `PROCESSING_DETAILS` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ;
"""
    cursor.execute(sql)
    con.commit()


def cleanup_schema():
    sql = "ALTER TABLE  `NETWORKS` DROP COLUMN  `FILENAME`, DROP COLUMN `ORIGINAL_FILENAME`, DROP COLUMN `UPLOAD_DATE`, DROP COLUMN  `PROCESSING_DETAILS`;"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE  `IDENTIFIERS` DROP COLUMN  `FILENAME`, DROP COLUMN  `ORIGINAL_FILENAME`, DROP COLUMN  `UPLOAD_DATE`;"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE  `ATTRIBUTE_METADATA` DROP COLUMN  `FILENAME`, DROP COLUMN  `ORIGINAL_FILENAME`, DROP COLUMN  `PROCESSING_DETAILS`;"
    cursor.execute(sql)
    con.commit()
    
def update_data():

    print "db version"
    cursor.execute("insert into DB_VERSION (VERSION_ID) values ('0.3.0');")

    print "identifiers"
    cursor.execute("select ID, ORGANISM_ID, FILENAME, ORIGINAL_FILENAME, UPLOAD_DATE from IDENTIFIERS;")
    result = cursor.fetchall()    

    for record in result:
        identifier_id, organism_id, filename, original_filename, upload_date = record
        
        sql = "insert into DATAFILES (ORGANISM_ID, FILENAME, ORIGINAL_FILENAME, UPLOAD_DATE) values (%s, %s, %s, %s);"
        cursor.execute(sql, (organism_id, filename, original_filename, str(upload_date)))

        data_file_id = id = con.insert_id()
        sql = "update IDENTIFIERS set DATA_FILE_ID = %s where ID = %s;"
        cursor.execute(sql, (data_file_id, identifier_id))
        new_filename = clean_filename(os.path.basename(original_filename))
        new_filename = "%s/%s_%s" % (organism_id, data_file_id, new_filename)
        copy_to_new(filename, new_filename)
        sql = "update DATAFILES set FILENAME = %s where ID = %s;"
        cursor.execute(sql, [new_filename, data_file_id])
        con.commit()
        
    print "networks"
    cursor.execute("select ID, ORGANISM_ID, FILENAME, ORIGINAL_FILENAME, UPLOAD_DATE from NETWORKS")
    result = cursor.fetchall()
    
    for record in result:
        network_id, organism_id, filename, original_filename, upload_date = record
        sql = "insert into DATAFILES (ORGANISM_ID, FILENAME, ORIGINAL_FILENAME, UPLOAD_DATE) values (%s, %s, %s, %s);"
        cursor.execute(sql, (organism_id, filename, original_filename, str(upload_date)))

        data_file_id = id = con.insert_id()
        sql = "update NETWORKS set DATA_FILE_ID = %s where ID = %s;"
        cursor.execute(sql, (data_file_id, network_id))
        new_filename = clean_filename(os.path.basename(original_filename))
        new_filename = "%s/%s_%s" % (organism_id, data_file_id, new_filename)
        copy_to_new(filename, new_filename)
        sql = "update DATAFILES set FILENAME = %s where ID = %s;"
        cursor.execute(sql, [new_filename, data_file_id])
        con.commit()
              
    print "attribute metadata"
    sql = """select a.ID, n.ORGANISM_ID, a.FILENAME, a.ORIGINAL_FILENAME, n.UPLOAD_DATE
    from ATTRIBUTE_METADATA a, NETWORKS n
    where n.ATTRIBUTE_METADATA_ID = a.ID;
    """
     
    cursor.execute(sql)
    result = cursor.fetchall()
    
    for record in result:
        md_id, organism_id, filename, original_filename, upload_date = record
        sql = "insert into DATAFILES (ORGANISM_ID, FILENAME, ORIGINAL_FILENAME, UPLOAD_DATE) values (%s, %s, %s, %s);"
        cursor.execute(sql, (organism_id, filename, original_filename, str(upload_date)))

        data_file_id = id = con.insert_id()
        sql = "update ATTRIBUTE_METADATA set DATA_FILE_ID = %s where ID = %s;"
        cursor.execute(sql, (data_file_id, md_id))
        new_filename = clean_filename(os.path.basename(original_filename))
        new_filename = "%s/%s_%s" % (organism_id, data_file_id, new_filename)
        copy_to_new(filename, new_filename)
        sql = "update DATAFILES set FILENAME = %s where ID = %s;"
        cursor.execute(sql, [new_filename, data_file_id])
        con.commit()
        
    print "groups"
    cursor.execute("update GROUPS set GROUP_TYPE = 'ATTRIBUTE' where CODE = 'attrib';")
    con.commit()
    
def copy_to_new(old_filename, new_filename):
    old_path = os.path.join(STORAGE_DIR, old_filename)
    if not os.path.exists(old_path):
        raise Exception("does not exist: " + old_path)

    new_path = os.path.join(NEW_STORAGE_DIR, new_filename)
    dir, fn = os.path.split(new_path)
    print dir, fn
    if not os.path.exists(dir):
        os.makedirs(dir)
    shutil.copy(old_path, new_path)

def clean_filename(filename):
    new_filename = filename.replace(" ", "_")
    new_filename = re.sub("[^0-9a-zA-Z_\\-\\.]", "", new_filename)
    return new_filename

if __name__ == '__main__':
    alter_schema()
    update_data()
    cleanup_schema()
    print "done"
