# ticket-service
Install maven: https://maven.apache.org/download.cgi, then direct to the project file.

Use "mvn clean install" to build jar file.
Then use "java -cp ./target/ticket-service.jar com.mercury.ticketservice.TicketServiceApplication" to run this application


Set every seat with a priority, then use PriorityQueue to solve this problem.

Assumption: Best Seats are the seats which located at middle of the venue.
The venue is 9 * 33 the priority distribution like: (you can check it in the main method)

0 2 4 6 8 10 12 14 16 18 20 22 24 26 28 30 32 31 29 27 25 23 21 19 17 15 13 11 9 7 5 3 1

33 35 37 39 41 43 45 47 49 51 53 55 57 59 61 63 65 64 62 60 58 56 54 52 50 48 46 44 42 40 38 36 34

66 68 70 72 74 76 78 80 82 84 86 88 90 92 94 96 98 97 95 93 91 89 87 85 83 81 79 77 75 73 71 69 67

198 200 202 204 206 208 210 212 214 216 218 220 222 224 226 228 230 229 227 225 223 221 219 217 215 213 211 209 207 205 203 201 199

264 266 268 270 272 274 276 278 280 282 284 286 288 290 292 294 296 295 293 291 289 287 285 283 281 279 277 275 273 271 269 267 265

231 233 235 237 239 241 243 245 247 249 251 253 255 257 259 261 263 262 260 258 256 254 252 250 248 246 244 242 240 238 236 234 232

165 167 169 171 173 175 177 179 181 183 185 187 189 191 193 195 197 196 194 192 190 188 186 184 182 180 178 176 174 172 170 168 166

132 134 136 138 140 142 144 146 148 150 152 154 156 158 160 162 164 163 161 159 157 155 153 151 149 147 145 143 141 139 137 135 133

99 101 103 105 107 109 111 113 115 117 119 121 123 125 127 129 131 130 128 126 124 122 120 118 116 114 112 110 108 106 104 102 100

I think the last rows are better than the first three rows, so I set the different weight with each row.
