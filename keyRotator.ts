// Intelligent Multi-Provider API Key Rotation & Circuit Breaker Engine
// Handles auto-failover, cooldowns, and seamless rotation upon rate limits (429) or errors

export interface KeyEntry {
  id: string;
  provider: "groq" | "mistral" | "openrouter" | "gemini";
  key: string;
  maskedKey: string;
  priority: number;
  consecutiveErrors: number;
  cooldownUntil: number; // timestamp ms
  lastUsed: number;
  totalSuccess: number;
  totalErrors: number;
  lastErrorReason?: string;
  models: string[];
}

export interface RotationEvent {
  timestamp: string;
  fromProvider: string;
  toProvider: string;
  reason: string;
  fromKeyMasked?: string;
}

export class KeyRotator {
  private keys: Map<string, KeyEntry> = new Map();
  private rotationHistory: RotationEvent[] = [];
  private maxHistorySize = 50;

  constructor() {
    this.initializeDefaultKeys();
  }

  private mask(key: string): string {
    if (!key || key.length < 8) return "••••";
    const start = key.substring(0, Math.min(6, Math.floor(key.length / 3)));
    const end = key.substring(key.length - Math.min(4, Math.floor(key.length / 4)));
    return `${start}...${end}`;
  }

  public registerKey(
    provider: "groq" | "mistral" | "openrouter" | "gemini",
    rawKey: string,
    priority: number = 10,
    models: string[] = []
  ) {
    if (!rawKey || typeof rawKey !== "string") return;
    const cleanKey = rawKey.trim();
    if (!cleanKey) return;

    // Support comma-separated keys
    const individualKeys = cleanKey.split(",").map(k => k.trim()).filter(Boolean);
    for (const key of individualKeys) {
      const id = `${provider}-${key.slice(-8)}`;
      if (!this.keys.has(id)) {
        this.keys.set(id, {
          id,
          provider,
          key,
          maskedKey: this.mask(key),
          priority,
          consecutiveErrors: 0,
          cooldownUntil: 0,
          lastUsed: 0,
          totalSuccess: 0,
          totalErrors: 0,
          models
        });
      }
    }
  }

  public initializeDefaultKeys() {
    this.keys.clear();

    // 1. Groq Keys (Priority 1 - Ultra-Fast LPU)
    const groqEnv = process.env.GROQ_API_KEYS || process.env.GROQ_API_KEY;
    if (groqEnv) {
      this.registerKey("groq", groqEnv, 1, ["qwen/qwen3.8-27b", "openai/gpt-oss-120b", "openai/gpt-oss-20b", "allam-2-7b"]);
    }

    // 2. Mistral AI Keys (Priority 2 - High Capability)
    const mistralEnv = process.env.MISTRAL_API_KEYS || process.env.MISTRAL_API_KEY;
    if (mistralEnv) {
      this.registerKey("mistral", mistralEnv, 2, ["codestral-latest", "mistral-small-latest", "open-mistral-7b", "mistral-tiny"]);
    }

    // 3. OpenRouter Keys (Priority 3 - Free Tier Router)
    const openRouterEnv = process.env.OPENROUTER_API_KEYS || process.env.OPENROUTER_API_KEY;
    if (openRouterEnv) {
      this.registerKey("openrouter", openRouterEnv, 3, [
        "apodex/apodex-1.1-mini:free",
        "liquid/lfm-2.5-2.6b:free",
        "meta-llama/llama-3.2-3b-instruct:free",
        "deepseek/deepseek-r1:free",
        "qwen/qwen-2.5-coder-32b-instruct:free"
      ]);
    }

    // 4. Gemini (Priority 4 - Platform Provider)
    if (process.env.GEMINI_API_KEY) {
      this.registerKey("gemini", process.env.GEMINI_API_KEY, 4, ["gemini-3.1-flash-lite", "gemini-flash-latest"]);
    }
  }

  public getAvailableKey(provider?: "groq" | "mistral" | "openrouter" | "gemini"): KeyEntry | null {
    const now = Date.now();
    const candidateKeys = Array.from(this.keys.values()).filter(k => {
      if (provider && k.provider !== provider) return false;
      return k.cooldownUntil <= now;
    });

    if (candidateKeys.length === 0) return null;

    // Sort by priority ascending (lower number = higher priority), then by least recently used
    candidateKeys.sort((a, b) => {
      if (a.priority !== b.priority) return a.priority - b.priority;
      if (a.consecutiveErrors !== b.consecutiveErrors) return a.consecutiveErrors - b.consecutiveErrors;
      return a.lastUsed - b.lastUsed;
    });

    const chosen = candidateKeys[0];
    chosen.lastUsed = now;
    return chosen;
  }

  public getOrderedProviders(preferredProvider?: string): Array<"groq" | "mistral" | "openrouter" | "gemini"> {
    const defaultOrder: Array<"groq" | "mistral" | "openrouter" | "gemini"> = ["groq", "mistral", "openrouter", "gemini"];
    if (preferredProvider && defaultOrder.includes(preferredProvider as any)) {
      return [
        preferredProvider as any,
        ...defaultOrder.filter(p => p !== preferredProvider)
      ];
    }
    return defaultOrder;
  }

  public reportSuccess(keyId: string) {
    const entry = this.keys.get(keyId);
    if (entry) {
      entry.consecutiveErrors = 0;
      entry.totalSuccess += 1;
      entry.cooldownUntil = 0;
      delete entry.lastErrorReason;
    }
  }

  public reportFailure(keyId: string, statusCode: number | string, errorMessage: string) {
    const entry = this.keys.get(keyId);
    if (!entry) return;

    entry.totalErrors += 1;
    entry.consecutiveErrors += 1;
    entry.lastErrorReason = `[${statusCode}] ${errorMessage.slice(0, 150)}`;

    // Set intelligent cooldown based on status code
    let cooldownDurationMs = 30000; // 30s default
    if (statusCode === 429) {
      // Rate limited: cooldown for 60 seconds
      cooldownDurationMs = 60000;
    } else if (statusCode === 402 || statusCode === 403) {
      // Payment/Quota or Permission: cooldown for 5 minutes
      cooldownDurationMs = 300000;
    } else if (typeof statusCode === "number" && statusCode >= 500) {
      // Temporary server outage: 45s
      cooldownDurationMs = 45000;
    }

    entry.cooldownUntil = Date.now() + cooldownDurationMs;
  }

  public logRotation(fromProvider: string, toProvider: string, reason: string, fromKeyMasked?: string) {
    const event: RotationEvent = {
      timestamp: new Date().toLocaleTimeString("id-ID", { timeZone: "Asia/Jakarta" }) + " WIB",
      fromProvider,
      toProvider,
      reason,
      fromKeyMasked
    };
    this.rotationHistory.unshift(event);
    if (this.rotationHistory.length > this.maxHistorySize) {
      this.rotationHistory.pop();
    }
    console.log(`[Rotasi Otomatis] 🔄 Beralih dari ${fromProvider} (${fromKeyMasked || "Key"}) ke ${toProvider}: ${reason}`);
  }

  public getStatus() {
    const now = Date.now();
    const list = Array.from(this.keys.values()).map(k => {
      const isCoolingDown = k.cooldownUntil > now;
      const cooldownRemainingSeconds = isCoolingDown ? Math.ceil((k.cooldownUntil - now) / 1000) : 0;
      let status: "active" | "cooling_down" | "rate_limited" | "error" = "active";
      if (isCoolingDown) {
        status = k.lastErrorReason?.includes("429") ? "rate_limited" : "cooling_down";
      }

      return {
        id: k.id,
        provider: k.provider,
        maskedKey: k.maskedKey,
        priority: k.priority,
        status,
        models: k.models,
        consecutiveErrors: k.consecutiveErrors,
        cooldownRemainingSeconds,
        totalSuccess: k.totalSuccess,
        totalErrors: k.totalErrors,
        lastErrorReason: k.lastErrorReason || null
      };
    });

    return {
      totalKeys: list.length,
      activeKeysCount: list.filter(k => k.status === "active").length,
      keys: list,
      recentRotations: this.rotationHistory.slice(0, 15)
    };
  }

  public resetCooldowns() {
    for (const k of this.keys.values()) {
      k.cooldownUntil = 0;
      k.consecutiveErrors = 0;
      delete k.lastErrorReason;
    }
  }
}

export const globalKeyRotator = new KeyRotator();
