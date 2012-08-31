package backend_gps;

//----------------------------------------------------------------------
// Basic Block Creation
//  (1) pre-processing
//     Mark each sentence: Sequential, Contains Vertex, Begin Vertex
//  (2) make basic-blocks from this pre-processing result
//----------------------------------------------------------------------
public enum gps_gps_sentence_t //TOOD new name?
{
	GPS_TYPE_SEQ(1),
	GPS_TYPE_CANBE_VERTEX(2),
	GPS_TYPE_BEGIN_VERTEX(3),
	GPS_TYPE_IN_VERTEX(4);

	private int intValue;
	private static java.util.HashMap<Integer, gps_gps_sentence_t> mappings;
	private static java.util.HashMap<Integer, gps_gps_sentence_t> getMappings()
	{
		if (mappings == null)
		{
			synchronized (gps_gps_sentence_t.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, gps_gps_sentence_t>();
				}
			}
		}
		return mappings;
	}

	private gps_gps_sentence_t(int value)
	{
		intValue = value;
		gps_gps_sentence_t.getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static gps_gps_sentence_t forValue(int value)
	{
		return getMappings().get(value);
	}
}