package pokecube.core.client.gui;

import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glTranslatef;
import static pokecube.core.utils.PokeType.flying;
import static pokecube.core.utils.PokeType.getTranslatedName;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import pokecube.core.PokecubeItems;
import pokecube.core.client.Resources;
import pokecube.core.database.Database;
import pokecube.core.database.Pokedex;
import pokecube.core.database.PokedexEntry;
import pokecube.core.interfaces.IPokecube;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.PokecubeMod;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.PokeType;
import pokecube.core.utils.PokecubeSerializer;
import thut.api.entity.IMobColourable;

@SuppressWarnings("deprecation")
public class GuiChooseFirstPokemob extends GuiScreen
{

    public final static float                     POKEDEX_RENDER     = 1.5f;
    public static boolean                         options            = true;

    public static Integer[]                       starters;
    private static HashMap<Integer, EntityLiving> entityToDisplayMap = new HashMap<Integer, EntityLiving>();

    int                                           xSize              = 150;
    int                                           ySize              = 150;

    private float                                 yRenderAngle       = 10;

    private float                                 xRenderAngle       = 0;

    private float                                 yHeadRenderAngle   = 10;

    private float                                 xHeadRenderAngle   = 0;
    protected EntityPlayer                        entityPlayer       = null;
    protected PokedexEntry                        pokedexEntry       = null;
    int                                           index              = 0;

    boolean                                       fixed              = false;

    GuiButton                                     next;

    GuiButton                                     prev;
    GuiButton                                     choose;
    GuiButton                                     accept;

    GuiButton                                     deny;

    public GuiChooseFirstPokemob(Integer[] _starters)
    {
        super();
        fixed = true;
        if (_starters == null && starters == null)
        {
            _starters = PokecubeMod.core.getStarters();
            starters = PokecubeMod.core.getStarters();
        }
        if (starters == null || starters.length == 0)
        {
            starters = PokecubeMod.core.getStarters();
        }
        if (_starters == null)
        {
            _starters = starters;
        }
        if (starters.length == PokecubeMod.core.getStarters().length)
        {
            boolean same = true;
            for (int i = 0; i < starters.length; i++)
            {
                same = same && starters[i] == PokecubeMod.core.getStarters()[i];
            }
            if (same) fixed = false;
        }

        ArrayList<Integer> starts = new ArrayList<Integer>();
        for (int i = 0; i < _starters.length; i++)
        {
            if (_starters[i] != 0 && !starts.contains(_starters[i]))
            {
                starts.add(_starters[i]);
            }
        }
        GuiChooseFirstPokemob.starters = starts.toArray(new Integer[0]);
        entityPlayer = FMLClientHandler.instance().getClientPlayerEntity();
    }

    public GuiChooseFirstPokemob(Integer[] _starters, boolean fixed)
    {
        this(_starters);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        int n = guibutton.id;
        if (n == 1)
        {
            index++;
            if (index >= starters.length) index = 0;
        }
        if (n == 2)
        {
            if (index > 0) index--;
            else index = starters.length - 1;
        }
        if (n == 4)
        {
            int pokedexNb = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);
            try
            {
                outputStream.writeInt(pokedexNb);
                outputStream.writeBoolean(fixed);
                PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubeServerPacket.CHOOSE1ST,
                        bos.toByteArray());
                PokecubePacketHandler.sendToServer(packet);
                if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
                    PokecubeSerializer.instance.setHasStarter(entityPlayer);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            mc.thePlayer.closeScreen();
            options = true;
        }
        if (n == 5)
        {
            next.visible = true;
            prev.visible = true;
            choose.visible = true;
            accept.visible = false;
            deny.visible = false;
            options = true;
            fixed = true;
        }
        if (n == 3)
        {
            int pokedexNb = pokedexEntry.getPokedexNb();
            ByteArrayOutputStream bos = new ByteArrayOutputStream(8);
            DataOutputStream outputStream = new DataOutputStream(bos);
            try
            {
                outputStream.writeInt(pokedexNb);
                outputStream.writeBoolean(fixed);
                PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubeServerPacket.CHOOSE1ST,
                        bos.toByteArray());
                PokecubePacketHandler.sendToServer(packet);
                if (FMLCommonHandler.instance().getMinecraftServerInstance() == null)
                    PokecubeSerializer.instance.setHasStarter(entityPlayer);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            mc.thePlayer.closeScreen();
        }
    }

    /** Returns true if this GUI should pause the game when it is displayed in
     * single-player */
    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void drawScreen(int i, int j, float f)
    {
        try
        {
            this.drawDefaultBackground();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        super.drawScreen(i, j, f);

        if (!options)
        {
            drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.pokemob.choose1st.override"),
                    (width / 2), 17, 0xffffff);
            return;
        }
        pokedexEntry = Database.getEntry(starters[index % starters.length]);

        if (pokedexEntry == null) pokedexEntry = Pokedex.getInstance().getFirstEntry();
        if (pokedexEntry == null)
        {
            mc.thePlayer.closeScreen();
            return;
        }

        GL11.glPushMatrix();
        int i1 = 15728880;
        int j1 = i1 % 65536;
        int k1 = i1 / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j1 / 1.0F, k1 / 1.0F);

        drawCenteredString(fontRendererObj, StatCollector.translateToLocal("gui.pokemob.choose1st"), (width / 2), 17,
                0xffffff);

        drawCenteredString(fontRendererObj, pokedexEntry.getTranslatedName(), (width / 2), 45, 0xffffff);

        int n = 0;
        int m = 0;
        n = (width - xSize) / 2;
        m = (height - ySize) / 2;
        int l = 40;
        int k = 150;

        if (pokedexEntry.getType2() == PokeType.unknown)
        {
            drawCenteredString(fontRendererObj, getTranslatedName(pokedexEntry.getType1()), (width / 2), 65,
                    pokedexEntry.getType1().colour);
        }
        else
        {
            drawCenteredString(fontRendererObj, getTranslatedName(pokedexEntry.getType1()), (width / 2) - 20, 65,
                    pokedexEntry.getType1().colour);
            drawCenteredString(fontRendererObj, getTranslatedName(pokedexEntry.getType2()), (width / 2) + 20, 65,
                    pokedexEntry.getType2().colour);
        }
        GL11.glPushMatrix();

        mc.renderEngine.bindTexture(Resources.GUI_POKEMOB);

        GL11.glColor4f((255f / 255f), (0f / 255f), (0f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l, 0, 0, pokedexEntry.getStatHP(), 13);

        GL11.glColor4f((234f / 255f), (125f / 255f), (46f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l + 13, 0, 0, pokedexEntry.getStatATT(), 13);

        GL11.glColor4f((242f / 255f), (203f / 255f), (46f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l + 26, 0, 0, pokedexEntry.getStatDEF(), 13);

        GL11.glColor4f((102f / 255f), (140f / 255f), (234f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l + 39, 0, 0, pokedexEntry.getStatATTSPE(), 13);

        GL11.glColor4f((118f / 255f), (198f / 255f), (78f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l + 52, 0, 0, pokedexEntry.getStatDEFSPE(), 13);

        GL11.glColor4f((243f / 255f), (86f / 255f), (132f / 255f), 1.0F);
        this.drawTexturedModalRect(n + k, m + l + 65, 0, 0, pokedexEntry.getStatVIT(), 13);

        drawCenteredString(fontRendererObj, "VIT: ", (n + k) - 10, m + l + 3, 0x930000);
        drawCenteredString(fontRendererObj, "ATT: ", (n + k) - 10, m + l + 17, 0xAD5D22);
        drawCenteredString(fontRendererObj, "DEF: ", (n + k) - 10, m + l + 29, 0xB39622);
        drawCenteredString(fontRendererObj, "ATTSPE: ", (n + k) - 18, m + l + 42, 0x4C68AD);
        drawCenteredString(fontRendererObj, "DEFSPE: ", (n + k) - 18, m + l + 55, 0x57933A);
        drawCenteredString(fontRendererObj, "SPE: ", (n + k) - 10, m + l + 67, 0xB44062);
        GL11.glPopMatrix();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        renderItem(n + 00, m + 75, 40);
        renderMob();

        GL11.glPopMatrix();
    }

    private EntityLiving getEntityToDisplay()
    {
        EntityLiving pokemob = entityToDisplayMap.get(pokedexEntry.getPokedexNb());

        if (pokemob == null)
        {
            pokemob = (EntityLiving) PokecubeMod.core.createEntityByPokedexNb(pokedexEntry.getPokedexNb(),
                    entityPlayer.worldObj);

            if (pokemob != null)
            {
                entityToDisplayMap.put(pokedexEntry.getPokedexNb(), pokemob);
            }
        }

        return pokemob;
    }

    @Override
    public void initGui()
    {
        super.initGui();
        buttonList.clear();
        int xOffset = 0;
        int yOffset = 110;
        if (starters.length > 1)
        {
            String next = StatCollector.translateToLocal("tile.pc.next");
            buttonList.add(this.next = new GuiButton(1, width / 2 - xOffset + 65, height / 2 - yOffset, 50, 20, next));
            String prev = StatCollector.translateToLocal("tile.pc.previous");
            buttonList.add(this.prev = new GuiButton(2, width / 2 - xOffset - 115, height / 2 - yOffset, 50, 20, prev));
        }

        String choose = StatCollector.translateToLocal("gui.pokemob.select");
        buttonList.add(
                this.choose = new GuiButton(3, width / 2 - xOffset - 25, height / 2 - yOffset + 160, 50, 20, choose));

        buttonList.add(this.accept = new GuiButton(4, width / 2 - xOffset + 64, height / 2 - yOffset + 30, 50, 20,
                StatCollector.translateToLocal("gui.pokemob.accept")));
        buttonList.add(this.deny = new GuiButton(5, width / 2 - xOffset - 115, height / 2 - yOffset + 30, 50, 20,
                StatCollector.translateToLocal("gui.pokemob.deny")));

        if (options)
        {
            accept.visible = false;
            deny.visible = false;
        }
        else
        {
            next.visible = false;
            prev.visible = false;
            this.choose.visible = false;
        }

    }

    @Override
    protected void keyTyped(char par1, int par2) throws IOException
    {

        super.keyTyped(par1, par2);
        if (par2 == 1)
        {
            mc.thePlayer.closeScreen();
            return;
        }

    }

    public void renderItem(double x, double y, double z)
    {
        ItemStack item = PokecubeItems.getStack("pokecube");
        if (item.getItem() instanceof IPokecube)
        {
            glPushMatrix();
            GL11.glPushAttrib(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_BLEND);
            glTranslatef((float) x, (float) y, (float) z);
            glPushMatrix();
            glTranslatef(0.5F, 1.0f, 0.5F);
            glRotatef(-180, 1.0F, 0.0F, 0.0F);
            glRotatef(entityPlayer.worldObj.getWorldTime() * 2, 0.0F, 1.0F, 0.0F);
            glScalef(50f, 50f, 50f);

            GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);
            RenderHelper.disableStandardItemLighting();

            Minecraft.getMinecraft().getItemRenderer().renderItem(entityPlayer, item, TransformType.GUI);

            glPopMatrix();
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glPopAttrib();
            glPopMatrix();
        }
    }

    private void renderMob()
    {
        try
        {
            EntityLiving entity = getEntityToDisplay();

            float size = 0;
            int j = 0;
            int k = 0;

            IPokemob pokemob = null;
            if (entity instanceof IPokemob)
            {
                pokemob = (IPokemob) entity;
            }

            if (entity instanceof IPokemob)
            {
                pokemob.setShiny(false);
                pokemob.setSize(4);
            }

            if (entity instanceof IMobColourable) ((IMobColourable) pokemob).setRGBA(255, 255, 255, 255);
            size = Math.max(entity.width, entity.height);
            j = (width - xSize) / 2;
            k = (height - ySize) / 2;

            GL11.glPushMatrix();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_COLOR_MATERIAL);
            GL11.glTranslatef(j + 0, k + 90, 500F);
            float zoom = (float) (50F / Math.sqrt(size + 0.6));

            GL11.glScalef(-zoom, zoom, zoom);
            GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
            float f5 = (float) ((k + 75) - 50) - ySize;
            GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);

            RenderHelper.enableStandardItemLighting();
            GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-(float) Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
            entity.renderYawOffset = 0F;
            entity.rotationYaw = yHeadRenderAngle;
            entity.rotationPitch = xHeadRenderAngle;
            entity.rotationYawHead = entity.rotationYaw;
            GL11.glTranslatef(0.0F, (float) entity.getYOffset(), 0.0F);
            yRenderAngle = yRenderAngle + 0.15F;
            GL11.glRotatef(yRenderAngle, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(xRenderAngle, 1.0F, 0.0F, 0.0F);

            entity.limbSwing = 0;
            entity.limbSwingAmount = 0;
            entity.onGround = ((IPokemob) entity).getType1() != flying && ((IPokemob) entity).getType2() != flying;
            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(entity, 0, 0, 0, 0, POKEDEX_RENDER);
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            GL11.glDisable(GL11.GL_COLOR_MATERIAL);
            GL11.glPopMatrix();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

}
