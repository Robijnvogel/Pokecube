package pokecube.core.database.abilities.c;

import pokecube.core.database.abilities.Ability;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.IPokemob.MovePacket;

public class ColorChange extends Ability
{
    @Override
    public void onMoveUse(IPokemob mob, MovePacket move)
    {
        if (mob == move.attacked && !move.pre)
        {
            mob.setType1(move.attackType);
        }
    }
}
