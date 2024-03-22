package genetics;

public interface AbstractMutationFactory{
   
	//mutate existing mutation, reverse boolean or change allele(A) or allele value(s,h) 
	 Mutation mutate(Mutation mutation);

	 //create new mutation where previous one didn't exist 
	 Mutation mutate();
	 
	 //create at inialistation
	 Mutation create();
}