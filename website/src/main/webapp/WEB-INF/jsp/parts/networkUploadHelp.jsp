<div>
    <div id="uploadHelpDialog" title="Upload network help">
        <p>To upload your own network, choose "Upload..." in the advanced options. Your network 
        must be for one of the GeneMANIA supported organisms and must use one of the supported 
        Gene IDs, including <b>Entrez</b>, <b>Ensembl</b>, <b>UniProt/SwissProt</b>, <b>RefSeq</b>,
        or gene symbols.</p>
    
        <p><b>File format:</b> tab delimited text (this is a "Save As" option in Excel). The file should
        contain either two or three columns and each line corresponds to one interaction.
        Self-interactions are ignored.</p>
        
        <p><b>Format of two column file:</b></p>
        
        <code>
            Gene ID &lt;tab&gt; Gene ID
        </code>
        
        <p><b>Format of three column file:</b></p>
        
        <code>
            Gene ID &lt;tab&gt; Gene ID &lt;tab&gt; Score
        </code>
        
        <p><b>Scores:</b>  A score is a positive number that corresponds to a measure of confidence or 
        strength in the interaction:  A higher score means more confidence or strength.  A 
        two-column file&#8212;a protein-protein interaction network, for example&#8212;is equivalent to a
        three-column file with the score set to 1.0 for each line.</p>
        
        <p>We recommend only including
        interactions above a threshold to reduce the size of the network, especially for correlation
        based scores.  The <a target="_blank" href="http://pages.genemania.org/help">help section</a> describes how we assigned
        interaction scores for the GeneMANIA network.</p>
    </div>
</div>
        