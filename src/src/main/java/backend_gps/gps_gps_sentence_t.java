package backend_gps;

//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define TO_STR(X) #X
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define DEF_STRING(X) static const char *X = "X"
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP(CLASS, DESC) class CLASS : public gm_compile_step { private: CLASS() {set_description(DESC);}public: virtual void process(ast_procdef*p); virtual gm_compile_step* get_instance(){return new CLASS();} static gm_compile_step* get_factory(){return new CLASS();} };
//C++ TO JAVA CONVERTER NOTE: The following #define macro was replaced in-line:
///#define GM_COMPILE_STEP_FACTORY(CLASS) CLASS::get_factory()


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