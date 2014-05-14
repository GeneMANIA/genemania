
# from 0.1.1 -> 0.2.0

# file storage changes, make filename full path relative to base storage location


import pymysql, re, os, shutil

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
    sql = "ALTER TABLE  `NETWORKS` ADD  `TYPE` VARCHAR( 16 ) NOT NULL DEFAULT 'NETWORK';"
    cursor.execute(sql)
    con.commit()
    
    sql = "update FORMATS set NAME='network' where NAME='direct network';"
    cursor.execute(sql)
    con.commit()
    
    sql = "ALTER TABLE NETWORKS MODIFY FILENAME varchar(255) null;"
    cursor.execute(sql)
    con.commit()

    sql = "ALTER TABLE NETWORKS MODIFY ORIGINAL_FILENAME varchar(255) null;"
    cursor.execute(sql)
    con.commit()

    sql = "ALTER TABLE IDENTIFIERS MODIFY FILENAME varchar(255) null;"
    cursor.execute(sql)
    con.commit()

    sql = "ALTER TABLE IDENTIFIERS MODIFY ORIGINAL_FILENAME varchar(255) null;"
    cursor.execute(sql)
    con.commit()

    sql = "ALTER TABLE NETWORKS MODIFY FORMAT_ID int(11) null;"
    cursor.execute(sql)
    con.commit()

def add_new_records():
    sql = "insert into GROUPS (NAME, CODE) values ('Attributes', 'attrib');"
    cursor.execute(sql)
    con.commit()
    
def fix_file_names():
    
  
    print "identifiers"
    cursor.execute("select ID, ORGANISM_ID, FILENAME, ORIGINAL_FILENAME from IDENTIFIERS;")
    result = cursor.fetchall()    
    fix_recs = fix('identifiers', result)
    save_fix_idents(fix_recs)
    
    print "networks"
    cursor.execute("select ID, ORGANISM_ID, FILENAME, ORIGINAL_FILENAME from NETWORKS;")
    result = cursor.fetchall()
    fix_recs = fix('networks', result)
    save_fix_networks(fix_recs)
    
def save_fix_idents(fix_recs):
    print fix_recs

    for i in fix_recs:
        sql = "update IDENTIFIERS set FILENAME='%s' where ID = %s;" % (i[0], i[1])
        print sql
        cursor.execute(sql)
        con.commit()
    
def save_fix_networks(fix_recs):
    for i in fix_recs:
        sql = "update NETWORKS set FILENAME='%s' where ID = %s;" % (i[0], i[1])
        print sql
        cursor.execute(sql)
        con.commit()
     
def fix(code, records):
    fix_recs = []
    for id, oid, fn, ofn in records: # row id, org id, filename, orig filename
        print id, oid, fn, ofn
        clean = new_path(code, id, oid, ofn)
        copy_to_new(oid, fn, clean)
        fix_recs.append( (clean, id) )

    return fix_recs

def copy_to_new(oid, fn, clean):
    old_path = os.path.join(STORAGE_DIR, str(oid), fn)
    if not os.path.exists(old_path):
        raise Exception("does not exist: " + old_path)

    new_path = os.path.join(NEW_STORAGE_DIR, clean)
    dir, fn = os.path.split(new_path)
    print dir, fn
    if not os.path.exists(dir):
        os.makedirs(dir)
    shutil.copy(old_path, new_path)
    
def new_path(code, id, oid, ofn):
    ofn = os.path.basename(ofn)
    clean = ofn.replace(" ", "_")
    clean = re.sub("[^0-9a-zA-Z_\\-\\.]", "", clean)
    clean = '%s_%s' % (id, clean)
    clean = os.path.join(str(oid), code, clean )
    print "clean: ", clean
    return clean

if __name__ == '__main__':
    alter_schema()
    add_new_records()
    fix_file_names()
