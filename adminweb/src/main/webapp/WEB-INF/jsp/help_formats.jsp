<!DOCTYPE HTML>
<%@include file="header.jsp"%>

<html>
<body>

    <div class="container-fluid">
        <div class="row-fluid">
            <div class="span12">
                <%@include file="navbar_simple.jsp"%>
            </div>
        </div>
        <div class="row-fluid">

            <h2>File Formats</h2>

            <h3>Gene Identifiers</h3>

            <p>A list of identifiers such as Gene Name, Ensembl Gene ID, or Uniprot ID specific to a particular organism
            can be defined in a 3 column tab delimited text file in the format:
            
            <pre>Unique ID<small>&lt;tab&gt;</small>Gene Identifier<small>&lt;tab&gt;</small>Identifier Source</pre>
             
            <p>
            The Unique ID associates all records in the file that correspond to the same gene, and can be a temporary internal ID.</p>
            
            All networks that are loaded must identify interactions using gene identifiers from this list for the interaction
            to be recognized. An example identifier file fragment is</p>
<pre>Hs:12161991     ENSG00000000003 Ensembl Gene ID
Hs:12161991     ENSP00000362111 Ensembl Protein ID
Hs:12161991     ENSP00000409517 Ensembl Protein ID
Hs:12161991     ENST00000373020 Ensembl Transcript ID
Hs:12161991     ENST00000431386 Ensembl Transcript ID
Hs:12161991     ENST00000494424 Ensembl Transcript ID
Hs:12161991     ENST00000496771 Ensembl Transcript ID
Hs:12161991     7105    Entrez Gene ID
Hs:12161991     TSPAN6  Gene Name
Hs:12161991     NP_003261       RefSeq Protein ID
Hs:12161991     NM_003270       RefSeq mRNA ID
Hs:12161991     T245    Synonym
Hs:12161991     TM4SF6  Synonym
Hs:12161991     TSPAN-6 Synonym
Hs:12161991     O43657  Uniprot ID
Hs:12161991     TSN6_HUMAN      Uniprot ID
Hs:12161992     ENSG00000000005 Ensembl Gene ID
Hs:12161992     ENSP00000362122 Ensembl Protein ID
Hs:12161992     ENST00000373031 Ensembl Transcript ID
Hs:12161992     ENST00000485971 Ensembl Transcript ID
Hs:12161992     64102   Entrez Gene ID
Hs:12161992     TNMD    Gene Name
Hs:12161992     NP_071427       RefSeq Protein ID
Hs:12161992     NM_022144       RefSeq mRNA ID
Hs:12161992     BRICD4  Synonym
Hs:12161992     CHM1L   Synonym
Hs:12161992     myodulin        Synonym
Hs:12161992     tendin  Synonym
Hs:12161992     Q9H2S6  Uniprot ID</pre>

            <h3>Networks</h3>
           
            <p>
            Networks can be uploaded in several formats. Direct networks are represented as gene identifier pairs with an optional score,
            one per tab-delimited line of the file:
             </p>
                        
            <pre>Gene ID<small>&lt;tab&gt;</small>Gene ID<small>&lt;tab&gt;</small>Score</pre>

            <p>
            or
            </p>
            
            <pre>Gene ID<small>&lt;tab&gt;</small>Gene ID</pre>

            <p>
            All the records in the file must use one or the other format. The score must be a positive value, with larger values
            representing strong interactions. 
            </p>
            
            <p>
            Networks can also be computed by correlation from expression data, where each line of the file is a gene identifier followed by a list
            of numerical expression levels for each condition.
            </p>
            
            <pre>Gene ID<small>&lt;tab&gt;</small>Level 1<small>&lt;tab&gt;</small>Level 2 ...</pre>

            <h3>Attributes</h3>
            <p>
            As a particular variation of networks, attributes are loaded from files that associate genes with features
            such as protein domains. These are converted into sets of networks, one per feature or attribute,
            where all genes having that attribute are connected to one another.
            
            <p>            
            Attribute files can be uploaded as tab delimited text files with the extension '.gmt' in <a
                    href="http://www.broadinstitute.org/cancer/software/gsea/wiki/index.php/Data_formats#GMT:_Gene_Matrix_Transposed_file_format_.28.2A.gmt.29">GMT</a>
                format. Alternatively text tab delimited text files (with any other extension) can be provded
                where each record has the format
            </p>
                
            <pre>Gene ID<small>&lt;tab&gt;</small>Attribute ID<small>&lt;tab&gt;</small>Attribute ID ...</pre>

            <h3>Attributes Descriptions</h3>
            <p>
            To provide additional descriptive information about attributes, optional attribute description files
            can be provded as 2 or 3 columns of tab delimited text, with each line containing an accession id, attribute name, and
                optional description.
            </p>
            
            <p>
            An example description record for protein domain metadata is:
            </p>
            
            <pre>IPR020610<small>&lt;tab&gt;</small>Thiolase_AS<small>&lt;tab&gt;</small>Thiolase, active site</pre>
        </div>
    </div>

</body>
</html>