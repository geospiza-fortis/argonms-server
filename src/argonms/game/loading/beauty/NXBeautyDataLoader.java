package argonms.game.loading.beauty;

import java.io.File;
import java.io.IOException;
import us.aaronweiss.pkgnx.EagerNXFile;
import us.aaronweiss.pkgnx.NXFile;
import us.aaronweiss.pkgnx.NXNode;

public class NXBeautyDataLoader extends BeautyDataLoader {

    private final String dataPath;

    protected NXBeautyDataLoader(String wzPath) {
        this.dataPath = wzPath;
    }

    @Override
    public boolean loadAll() {
        String dir = dataPath + "Character.nx" + File.separatorChar;

        try {
            NXFile file = new EagerNXFile(dir);
            for (NXNode baseNode : file.resolve("Hair")) {
                short id = Short.parseShort(baseNode.getName().replace(".img", ""));
                hairStyles.add(id);
            }

            for (NXNode baseNode : file.resolve("Face")) {
                short id = Short.parseShort(baseNode.getName().replace(".img", ""));
                eyeStyles.add(id);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

}
