import java.util.ArrayList;

import ciir.umass.edu.learning.tree.Ensemble;
import ciir.umass.edu.learning.tree.RegressionTree;

public class SolrLTROutputEnsemble extends Ensemble{
	

	public SolrLTROutputEnsemble(Ensemble e)
	{
		super(e);
	}
	
	public String toSolrLtrJsonOuput(){
		
		return toString();
	}
	
	public String toString()
	{
		String strRep = "<ensemble>" + "\n";
		for(int i=0;i<trees.size();i++)
		{
			strRep += "\t<tree id=\"" + (i+1) + "\" weight=\"" + weights.get(i) + "\">" + "\n";
			strRep += trees.get(i).toString("\t\t");
			strRep += "\t</tree>" + "\n";
		}
		strRep += "</ensemble>" + "\n";
		return strRep;
	}

}
