import java.util.*;
import java.io.*;

public class createIntermediateData{

    private static final String FB = "fb30";
	private static final String FB_PATH = "dat/" + FB + "/train.txt";
	private static final String ENTITY_IDX = "dat/" + FB +"-intermediate/entity.idx";
	private static final String RELATION_IDX = "dat/" + FB +"-intermediate/relation.idx";
	private static final String TRIPLE_IDX = "dat/" + FB +"-intermediate/triple.idx";
	private static final String INC_LEFT = "dat/" + FB +"-intermediate/inc_left.list";	//eg. e_1: t_2, t_3, t_4,
	private static final String INC_RIGHT = "dat/" + FB + "-intermediate/inc_right.list";	//eg. e_2: t_2, t_5,
	private static final String INC_REL = "dat/" + FB + "-intermediate/inc_rel.list"; 	//eg. r_1: t_3,...
	private static List<String> eIdList;
	private static List<String> rIdList;
	
	public static void main(String[] args) throws Exception
	{
		createERIndex();
		getIDList();
		createTripleIdx();
		findTripleListForEntity();
	}
	

	// write inc_left.list, inc_right.list, inc_rel.list
	private static void findTripleListForEntity( ) throws Exception
	{
		System.out.println( "Start find triples given entity index..." );
		BufferedReader br = new BufferedReader( new FileReader( TRIPLE_IDX ) );
		List< List< String > > leftList = new LinkedList< List<String> >();
		List< List< String > > rightList = new LinkedList< List<String> >();
		List< List< String > > relList = new LinkedList< List<String> >();
		
		for( int i = 0; i < eIdList.size(); ++i )
		{
			List<String> tmp = new LinkedList<String>();
			leftList.add( tmp );
			List<String> tmp2 = new LinkedList<String>();
			rightList.add(tmp2);
		}

		for( int i = 0; i < rIdList.size(); ++i )
		{
			List<String> tmp = new LinkedList<String>();
			relList.add( tmp );
		}

		String line = null;
		int triple_cnter = 1;
		System.out.println( "-- Read triple index --");
		while( ( line = br.readLine() ) != null )
		{
			String[] splitted = line.split("\\s+");
/*			System.out.println( splitted[ 0 ] );
			System.out.println( splitted[ 1 ] );
			System.out.println( splitted[ 2 ] ); */
			List<String> tmp_left = leftList.get( Integer.parseInt( splitted[ 0 ] ) - 1 );
			tmp_left.add( Integer.toString( triple_cnter ) );
			List<String> tmp_right = rightList.get( Integer.parseInt( splitted[ 2 ] ) - 1 );
			tmp_right.add( Integer.toString( triple_cnter ) );
			List<String> tmp_rel = relList.get( Integer.parseInt( splitted[ 1 ] ) - 1 );
			tmp_rel.add( Integer.toString( triple_cnter ) );
			triple_cnter++;
		}
		br.close();

		BufferedWriter bw1 = new BufferedWriter( new FileWriter( INC_LEFT ) );
		BufferedWriter bw2 = new BufferedWriter( new FileWriter( INC_RIGHT ) );
		BufferedWriter bw3 = new BufferedWriter( new FileWriter( INC_REL ) );

		System.out.println( "-- write triples given left entity --" );
		int idx = 0;
		for( List<String> tmp : leftList )
		{	
			idx++;
			if( tmp.size() == 0 )
				continue;	

			bw1.append( Integer.toString( idx ) + ':' );
			for( String st : tmp )
			{
				bw1.append( st );
				bw1.append( '\t' );
			}
			bw1.newLine();
		}
		bw1.close();
		idx = 0;
		System.out.println( "-- write triples given right entity --" );

		for( List<String> tmp : rightList )
		{
			idx++;
			if( tmp.size() == 0 )
				continue;

			bw2.append( Integer.toString( idx ) + ':' );
			for( String st : tmp )
			{
				bw2.append( st );
				bw2.append( '\t' );
			}
			bw2.newLine();
		}
		bw2.close();


		idx = 0;
		System.out.println( "-- write triples given relation --" );

		for( List<String> tmp : relList )
		{
			idx++;
			if( tmp.size() == 0 )
				continue;
			
			bw3.append( Integer.toString( idx )  + ':');
			for( String st : tmp )
			{
				bw3.append( st );
				bw3.append( '\t' );
			}
			bw3.newLine();
		}
		bw3.close();
		System.out.println( "Finish triple finding" );

	}

  	// read entity.idx and relation.idx, write them into eIdList and rIdList
	private static void getIDList()throws Exception
	{

		System.out.println( "Start getting entity and relation id... " );
		eIdList = new LinkedList<String>();
		rIdList = new LinkedList<String>();
		BufferedReader br_e = new BufferedReader(new FileReader(ENTITY_IDX));
		BufferedReader br_r = new BufferedReader(new FileReader(RELATION_IDX));
		String line = null;
		
		while( ( line = br_e.readLine() ) != null)
			eIdList.add( line.split("\\s+")[ 1 ] );
		
		br_e.close();
		
		while( ( line = br_r.readLine() ) != null )
			rIdList.add( line.split( "\\s+" )[ 1 ] );
		
		br_r.close();
		System.out.println( "Finsih getting entity and relation id... " );
		
	}

	// write triple_idx.txt	
	private static void createTripleIdx() throws Exception
	{
		System.out.println("Create triple idx...");
		BufferedReader br = new BufferedReader(new FileReader(FB_PATH));
		BufferedWriter bw_t = new BufferedWriter(new FileWriter(TRIPLE_IDX));
		
		String line = null;
		while( ( line = br.readLine() ) != null )
		{
			String[] splitted = line.split("\\s+");
/*			System.out.println( eIdList.indexOf( splitted[ 0 ] ));
			System.out.println( splitted[ 0 ] ); */
			bw_t.append( Integer.toString( eIdList.indexOf( splitted[ 0 ] ) + 1 ) + "\t" );
			bw_t.append( Integer.toString( rIdList.indexOf( splitted[ 1 ] ) + 1 ) + "\t" );
			bw_t.append( Integer.toString( eIdList.indexOf( splitted[ 2 ] ) + 1 ) + "\t" + 1 );
			bw_t.newLine();
		}

		bw_t.close();
	}

	// read Freebase and write entity.idx, relation.idx 
	private static void createERIndex() throws Exception
	{
		System.out.println("Creating Entity idx and Relation idx...");
		BufferedReader br = new BufferedReader(new FileReader(FB_PATH));
		BufferedWriter bw_e = new BufferedWriter(new FileWriter(ENTITY_IDX));
		BufferedWriter bw_r = new BufferedWriter(new FileWriter(RELATION_IDX));
		HashSet<String> eIdSet = new HashSet<String>();
		HashSet<String> rIdSet = new HashSet<String>();
		
		
		String line = null;
		while( ( line = br.readLine() ) != null )
		{
			String[] splitted = line.split("\\s+");
			eIdSet.add( splitted[ 0 ] );
			rIdSet.add( splitted[ 1 ] );
			eIdSet.add( splitted[ 2 ] );
		}
		br.close();
		
		int cnter = 1;
		for( String st : eIdSet )
		{
			bw_e.append(Integer.toString( cnter ));
			cnter ++;
			bw_e.append("\t" + st);
			bw_e.newLine();
		}
		bw_e.close();
		
		cnter = 1;
		for( String st : rIdSet )
		{
			bw_r.append(Integer.toString( cnter ));
			cnter ++;
			bw_r.append("\t" + st);
			bw_r.newLine();
		}
		bw_r.close();
	}
}
