/*
 * Copyright 2017 github.com/kaaz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package emily.db.model;

import emily.main.ProgramVersion;

import java.sql.Timestamp;

public class OBotVersion {

    public int id = 0;
    public int major = 1;
    public int minor = 0;
    public int patch = 0;
    public Timestamp createdOn = null;
    public int published = 0;

    public ProgramVersion getVersion() {
        return new ProgramVersion(major, minor, patch);
    }
}
