package knightminer.ceramics.library.tank;

import knightminer.ceramics.tileentity.TileBarrel;

public class BarrelTank extends TileTank<TileBarrel> {

	public int renderOffset;

	public BarrelTank(int capacity, TileBarrel parent) {
		super(capacity, parent);
	}

	@Override
	public void onContentsChanged() {
		parent.onTankContentsChanged();
	}

	@Override
	protected void sendUpdate(int amount) {
		if(amount != 0) {
			renderOffset += amount;
		}
		super.sendUpdate(amount);
	}

	@Override
	public void setCapacity(int capacity) {
		super.setCapacity(capacity);
		renderOffset = 0; // don't render it lowering from a barrel above breaking, that looks dumb
	}
}
