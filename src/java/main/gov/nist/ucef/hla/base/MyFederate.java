package gov.nist.ucef.hla.base;

import hla.rti1516e.InteractionClassHandle;

public class MyFederate extends FederateBase {

	@Override
	public void beforeFederationCreate()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeFederationJoin()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeReadyToPopulate()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void beforeReadyToRun()
	{
		// TODO Auto-generated method stub
	}

	@Override
	public boolean step( double currentTime )
	{
		System.out.println( "sending interaction...");
        InteractionClassHandle interactionHandle = rtiamb.getInteractionClassHandle( "HLAinteractionRoot.CustomerTransactions.FoodServed.DrinkServed" );
		HLAInteraction interaction = new HLAInteraction( interactionHandle, null );
		interaction.send(null, null);
		return false;
	}

	@Override
	public void beforeReadyToResign()
	{
	}

	@Override
	public void receiveObjectRegistration( HLAObject hlaObject )
	{
		System.out.println("receiveObjectRegistration");
	}

	@Override
	public void receiveAttributeReflection( HLAObject hlaObject, double time )
	{
		System.out.println("receiveAttributeReflection");
	}

	@Override
	public void receiveInteraction( HLAInteraction hlaInteraction, double time )
	{
		System.out.println("receiveInteraction");
	}
}
