package application.common.enums;

public enum EPolicy 
{
	RM,
	RM_improved,
	EDF,
	LLED, /* EDF, where ties broken by laxity */ 
	EDLL /* Least-Laxity, ties broken by deadline */,
	Random,
	FP_FIFO,
	NPFederated,
	Federated, /* preemptive Federate */
	RMNecessaryTest, /* a necessary condition for RM feasibility */
	PReserved,
	DynamicFederated,
	DynamicFederated_BFS,
	FP_Harmonic,  /* The FP analytical approach, when tasks are harmonic */
	Partitioned_WF_UTIL, /* Casini et al., 2018 */ 
	Partitioned_WF_ALGO, /* Casini et al., 2018 */
	Partitioned_BF_ALGO,
	Partitioned_FF_ALGO,
	PARTITIONED,
	Federated_Lazy,
	// Exhaustive /* Exhaustive search */
}
