package backend_gps;

//----------------------------------------------------------------------
// Basic Block Creation
//  (1) pre-processing
//     Mark each sentence: Sequential, Contains Vertex, Begin Vertex
//  (2) make basic-blocks from this pre-processing result
//----------------------------------------------------------------------
public enum gps_gps_sentence { // TODO new name?
	GPS_TYPE_SEQ, //
	GPS_TYPE_CANBE_VERTEX, //
	GPS_TYPE_BEGIN_VERTEX, //
	GPS_TYPE_IN_VERTEX;
}